package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.TicketSales;
import com.myce.dashboard.service.expo.TicketStatsService;
import com.myce.dashboard.service.expo.mapper.TicketSalesMapper;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketStatsServiceImpl implements TicketStatsService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final TicketSalesMapper ticketSalesMapper;
    
    private static final String REDIS_KEY_PREFIX = "expo:stats:";
    private static final int HEAVY_CACHE_TTL_MINUTES = 10;  // 무거운 쿼리용

    @Override
    public List<TicketSales> getTicketSalesDetail(Long expoId) {
        return getCachedValueOrCompute(
            expoId + ":ticket_sales:v5",
            () -> getTicketSalesDetailFromDB(expoId),
            List.class,
            HEAVY_CACHE_TTL_MINUTES
        );
    }

    @Override
    public void refreshTicketCache(Long expoId) {
        log.info("티켓 통계 캐시 갱신 시작 - ExpoId: {}", expoId);
        
        // 캐시 키 삭제 후 다음 조회 시 자동으로 갱신되도록 함
        String ticketSalesKey = REDIS_KEY_PREFIX + expoId + ":ticket_sales:v5";
        
        redisTemplate.delete(ticketSalesKey);
        
        log.info("티켓 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    @Override
    public void clearTicketCache(Long expoId) {
        log.info("티켓 통계 캐시 완전 삭제 시작 - ExpoId: {}", expoId);
        
        // 모든 티켓 관련 캐시 키 삭제
        String ticketSalesKey = REDIS_KEY_PREFIX + expoId + ":ticket_sales:v5";
        
        redisTemplate.delete(ticketSalesKey);
        
        log.info("티켓 통계 캐시 완전 삭제 완료 - ExpoId: {}", expoId);
    }
    
    // === 헬퍼 메서드들 ===
    
    private List<TicketSales> getTicketSalesDetailFromDB(Long expoId) {
        List<Object[]> results = reservationRepository.getTicketSalesDetailByExpoId(expoId);
        return ticketSalesMapper.mapFromQueryResults(results);
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