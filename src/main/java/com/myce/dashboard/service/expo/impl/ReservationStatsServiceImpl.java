package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.*;
import com.myce.dashboard.service.expo.ReservationStatsService;
import com.myce.dashboard.service.expo.mapper.DailyReservationMapper;
import com.myce.dashboard.service.expo.mapper.GenderStatsMapper;
import com.myce.dashboard.service.expo.mapper.AgeGroupStatsMapper;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.entity.Expo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationStatsServiceImpl implements ReservationStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final ExpoRepository expoRepository;
    private final DailyReservationMapper dailyReservationMapper;
    private final GenderStatsMapper genderStatsMapper;
    private final AgeGroupStatsMapper ageGroupStatsMapper;

    private static final String REDIS_KEY_PREFIX = "expo:stats:";
    private static final int CACHE_TTL_MINUTES = 3;         // 실시간 데이터용
    private static final int HEAVY_CACHE_TTL_MINUTES = 10;  // 무거운 쿼리용

    @Override
    public ReservationStats getReservationStats(Long expoId) {
        // Redis 우선 조회 후 없으면 DB 조회 + 캐시 저장 (실시간성 중요)
        Long todayReservations = getCachedValueOrCompute(
                expoId + ":reservations:today:v3",
                () -> {
                    LocalDate today = LocalDate.now();
                    LocalDateTime startOfDay = today.atStartOfDay();
                    LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();
                    return reservationRepository.countTodayReservationsByExpoId(expoId, startOfDay, startOfNextDay);
                },
                Long.class,
                CACHE_TTL_MINUTES
        );

        List<DailyReservation> weeklyReservations = getWeeklyReservations(expoId);

        // RDB에서 직접 조회할 데이터 (정확성 중요) - 캐싱 적용
        Long totalReservations = getCachedValueOrCompute(
                expoId + ":total_reservations:v5",
                () -> reservationRepository.countTotalReservationsByExpoId(expoId),
                Long.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        GenderStats genderStats = getCachedValueOrCompute(
                expoId + ":gender_stats",
                () -> getGenderStats(expoId),
                GenderStats.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        AgeGroupStats ageGroupStats = getCachedValueOrCompute(
                expoId + ":age_stats",
                () -> getAgeGroupStats(expoId),
                AgeGroupStats.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        return ReservationStats.builder()
                .todayReservations(todayReservations)
                .weeklyReservations(weeklyReservations)
                .totalReservations(totalReservations)
                .genderStats(genderStats)
                .ageGroupStats(ageGroupStats)
                .dataSource("mixed")
                .build();
    }

    @Override
    public List<DailyReservation> getWeeklyReservationsByDateRange(Long expoId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = reservationRepository.countReservationsByDateRange(expoId, startDateTime, endDateTime);
        return dailyReservationMapper.mapFromQueryResults(results, startDate, endDate);
    }

    @Override
    public void refreshReservationCache(Long expoId) {
        log.info("예약 통계 캐시 갱신 시작 - ExpoId: {}", expoId);

        // 캐시 키 삭제 후 다음 조회 시 자동으로 갱신되도록 함
        String todayKey = REDIS_KEY_PREFIX + expoId + ":reservations:today:v3";
        String totalKey = REDIS_KEY_PREFIX + expoId + ":total_reservations:v5";
        String genderKey = REDIS_KEY_PREFIX + expoId + ":gender_stats";
        String ageKey = REDIS_KEY_PREFIX + expoId + ":age_stats";

        redisTemplate.delete(todayKey);
        redisTemplate.delete(totalKey);
        redisTemplate.delete(genderKey);
        redisTemplate.delete(ageKey);

        log.info("예약 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    @Override
    public void clearReservationCache(Long expoId) {
        log.info("예약 통계 캐시 완전 삭제 시작 - ExpoId: {}", expoId);

        // 모든 예약 관련 캐시 키 삭제
        String todayKey = REDIS_KEY_PREFIX + expoId + ":reservations:today:v3";
        String totalKey = REDIS_KEY_PREFIX + expoId + ":total_reservations:v5";
        String genderKey = REDIS_KEY_PREFIX + expoId + ":gender_stats:v2";
        String ageKey = REDIS_KEY_PREFIX + expoId + ":age_stats:v2";

        redisTemplate.delete(todayKey);
        redisTemplate.delete(totalKey);
        redisTemplate.delete(genderKey);
        redisTemplate.delete(ageKey);

        log.info("예약 통계 캐시 완전 삭제 완료 - ExpoId: {}", expoId);
    }

    @Override
    public LocalDate[] getExpoDisplayDateRange(Long expoId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new RuntimeException("박람회를 찾을 수 없습니다."));

        return new LocalDate[]{expo.getDisplayStartDate(), expo.getDisplayEndDate()};
    }

    // === 헬퍼 메서드들 ===

    private List<DailyReservation> getWeeklyReservations(Long expoId) {
        // 박람회 게시 기간 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new RuntimeException("박람회를 찾을 수 없습니다."));

        LocalDate displayStart = expo.getDisplayStartDate();
        LocalDate displayEnd = expo.getDisplayEndDate();
        LocalDate today = LocalDate.now();

        // 기본 7일 범위 계산 (오늘부터 역산)
        LocalDate endDate = today.isAfter(displayEnd) ? displayEnd : today;
        LocalDate startDate = endDate.minusDays(6);

        // 게시 기간을 벗어나는 경우 조정
        if (startDate.isBefore(displayStart)) {
            startDate = displayStart;
            // 게시 기간이 7일보다 짧은 경우 전체 기간으로 설정
            if (displayEnd.isBefore(startDate.plusDays(6))) {
                endDate = displayEnd;
            }
        }

        // 실제 데이터 조회
        return getWeeklyReservationsByDateRange(expoId, startDate, endDate);
    }

    private GenderStats getGenderStats(Long expoId) {
        List<Object[]> genderResults = reserverRepository.countReserversByGender(expoId);
        return genderStatsMapper.mapFromQueryResults(genderResults);
    }

    private AgeGroupStats getAgeGroupStats(Long expoId) {
        List<Object[]> ageResults = reserverRepository.countReserversByAgeGroup(expoId);
        return ageGroupStatsMapper.mapFromQueryResults(ageResults);
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