package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.CheckinStats;
import com.myce.dashboard.dto.expo.HourlyCheckin;
import com.myce.dashboard.service.expo.CheckinStatsService;
import com.myce.dashboard.service.expo.mapper.HourlyCheckinMapper;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinStatsServiceImpl implements CheckinStatsService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final QrCodeRepository qrCodeRepository;
    private final ReservationRepository reservationRepository;
    private final HourlyCheckinMapper hourlyCheckinMapper;
    
    private static final String REDIS_KEY_PREFIX = "expo:stats:";
    private static final int CACHE_TTL_MINUTES = 3;         // 실시간 데이터용
    
    @Override
    public CheckinStats getCheckinStats(Long expoId) {
        // Redis 우선 조회 후 없으면 DB 조회 + 캐시 저장 (실시간성 중요)
        Long reservedTickets = getCachedValueOrCompute(
            expoId + ":checkin:reserved:v6",
            () -> reservationRepository.countTotalReservationsByExpoId(expoId),
            Long.class,
            CACHE_TTL_MINUTES
        );
        
        Long qrCheckinSuccess = getCachedValueOrCompute(
            expoId + ":checkin:success:v4",
            () -> qrCodeRepository.countSuccessfulCheckinsByExpoId(expoId),
            Long.class,
            CACHE_TTL_MINUTES
        );
        
        Float checkinProgress = 0f;
        if (reservedTickets != null && qrCheckinSuccess != null && reservedTickets > 0) {
            checkinProgress = (float) qrCheckinSuccess / reservedTickets * 100;
        }
        
        List<HourlyCheckin> hourlyCheckins = getHourlyCheckins(expoId);
        
        return CheckinStats.builder()
                .reservedTickets(reservedTickets)
                .qrCheckinSuccess(qrCheckinSuccess)
                .checkinProgress(checkinProgress)
                .hourlyCheckins(hourlyCheckins)
                .dataSource("redis")
                .build();
    }

    @Override
    public void refreshCheckinCache(Long expoId) {
        log.info("체크인 통계 캐시 갱신 시작 - ExpoId: {}", expoId);
        
        // 캐시 키 삭제 후 다음 조회 시 자동으로 갱신되도록 함
        String reservedKey = REDIS_KEY_PREFIX + expoId + ":checkin:reserved:v6";
        String successKey = REDIS_KEY_PREFIX + expoId + ":checkin:success:v4";
        
        redisTemplate.delete(reservedKey);
        redisTemplate.delete(successKey);
        
        log.info("체크인 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    @Override
    public void clearCheckinCache(Long expoId) {
        log.info("체크인 통계 캐시 완전 삭제 시작 - ExpoId: {}", expoId);

        // 모든 체크인 관련 캐시 키 삭제
        String reservedKey = REDIS_KEY_PREFIX + expoId + ":checkin:reserved:v6";
        String successKey = REDIS_KEY_PREFIX + expoId + ":checkin:success:v4";
        
        redisTemplate.delete(reservedKey);
        redisTemplate.delete(successKey);
        
        log.info("체크인 통계 캐시 완전 삭제 완료 - ExpoId: {}", expoId);
    }
    
    // === 헬퍼 메서드들 ===
    
    @Override
    public List<HourlyCheckin> getHourlyCheckinsByDate(Long expoId, LocalDate date) {
        List<Object[]> queryResults = qrCodeRepository.countHourlyCheckinsByExpoIdAndDate(expoId, date);
        return hourlyCheckinMapper.mapFromQueryResults(queryResults);
    }
    
    private List<HourlyCheckin> getHourlyCheckins(Long expoId) {
        // 기본적으로 오늘 날짜 사용
        return getHourlyCheckinsByDate(expoId, LocalDate.now());
    }
    
    /**
     * 캐시에서 값 조회
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedValue(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
            if (value == null) {
                return null;
            }
            
            if (type == Long.class && value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            } else if (type == Integer.class && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            } else if (type == BigDecimal.class && value instanceof Number) {
                return (T) new BigDecimal(value.toString());
            } else if (value instanceof LinkedHashMap) {
                // Redis에서 복합 객체가 LinkedHashMap으로 역직렬화되는 경우
                // 복합 객체는 캐시 미스로 처리하여 재계산하도록 함
                return null;
            }
            
            return (T) value;
        } catch (Exception e) {
            log.warn("Redis 조회 실패: {}", key, e);
            return null;
        }
    }
    
    /**
     * 캐시에서 값을 조회하고, 없으면 supplier로 계산한 후 캐시에 저장
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedValueOrCompute(String key, java.util.function.Supplier<T> supplier, 
                                         Class<T> type, int ttlMinutes) {
        try {
            // 1. 캐시에서 조회
            T cachedValue = getCachedValue(key, type);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // 2. 캐시 미스 시 실제 데이터 조회
            T computedValue = supplier.get();
            
            // 3. 캐시에 저장
            if (computedValue != null) {
                redisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + key,
                    computedValue,
                    ttlMinutes,
                    TimeUnit.MINUTES
                );
            }
            
            return computedValue;
        } catch (Exception e) {
            log.warn("캐시 조회/저장 실패: {}", key, e);
            // 캐시 실패 시 직접 조회
            return supplier.get();
        }
    }
}