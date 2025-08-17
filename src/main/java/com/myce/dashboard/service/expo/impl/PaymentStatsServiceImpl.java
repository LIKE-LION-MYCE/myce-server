package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.PaymentStats;
import com.myce.dashboard.service.expo.PaymentStatsService;
import com.myce.dashboard.service.expo.TicketStatsService;
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
public class PaymentStatsServiceImpl implements PaymentStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final TicketStatsService ticketStatsService;

    private static final String REDIS_KEY_PREFIX = "expo:stats:";
    private static final int CACHE_TTL_MINUTES = 1;         // 실시간 데이터용
    private static final int HEAVY_CACHE_TTL_MINUTES = 2;  // 무거운 쿼리용

    @Override
    public PaymentStats getPaymentStats(Long expoId) {
        // 결제 대기 건수 = 예약 상태가 CONFIRMED_PENDING인 건수 (Redis 캐시)
        Long pendingPayments = getCachedValueOrCompute(
                expoId + ":reservation:pending:v2",
                () -> reservationRepository.countPendingReservationsByExpoId(expoId),
                Long.class,
                CACHE_TTL_MINUTES
        );

        BigDecimal todayRevenue = getCachedValueOrCompute(
                expoId + ":payment:today_revenue:v2",
                () -> reservationRepository.sumTodayRevenueByExpoId(expoId, LocalDate.now()),
                BigDecimal.class,
                CACHE_TTL_MINUTES
        );

        // 결제 완료 건수 = 예약 상태가 CONFIRMED인 건수 (RDB 직접 조회, 건수 기준)
        Long completedPayments = getCachedValueOrCompute(
                expoId + ":completed_payments:v2",
                () -> reservationRepository.countConfirmedReservationsByExpoId(expoId),
                Long.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        // 환불 건수 = 예약 상태가 CANCELLED인 건수 (RDB 직접 조회, 건수 기준)
        Long refundedPayments = getCachedValueOrCompute(
                expoId + ":refunded_payments:v2",
                () -> reservationRepository.countCancelledReservationsByExpoId(expoId),
                Long.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        BigDecimal totalRevenue = getCachedValueOrCompute(
                expoId + ":total_revenue:v2",
                () -> reservationRepository.sumTotalRevenueByExpoId(expoId),
                BigDecimal.class,
                HEAVY_CACHE_TTL_MINUTES
        );

        return PaymentStats.builder()
                .pendingPayments(pendingPayments)
                .todayRevenue(todayRevenue)
                .completedPayments(completedPayments)
                .canceledPayments(0L)  // 취소 카드 제거하므로 0으로 설정
                .refundedPayments(refundedPayments)
                .totalRevenue(totalRevenue)
                .ticketSalesDetail(ticketStatsService.getTicketSalesDetail(expoId))
                .dataSource("mixed")
                .build();
    }

    @Override
    public void refreshPaymentCache(Long expoId) {
        log.info("결제 통계 캐시 갱신 시작 - ExpoId: {}", expoId);

        // 캐시 키 삭제 후 다음 조회 시 자동으로 갱신되도록 함
        String pendingKey = REDIS_KEY_PREFIX + expoId + ":reservation:pending:v2";
        String revenueKey = REDIS_KEY_PREFIX + expoId + ":payment:today_revenue:v2";
        String completedKey = REDIS_KEY_PREFIX + expoId + ":completed_payments:v2";
        String refundedKey = REDIS_KEY_PREFIX + expoId + ":refunded_payments:v2";
        String totalRevenueKey = REDIS_KEY_PREFIX + expoId + ":total_revenue:v2";

        redisTemplate.delete(pendingKey);
        redisTemplate.delete(revenueKey);
        redisTemplate.delete(completedKey);
        redisTemplate.delete(refundedKey);
        redisTemplate.delete(totalRevenueKey);

        // 티켓 통계 캐시도 갱신
        ticketStatsService.refreshTicketCache(expoId);

        log.info("결제 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    @Override
    public void clearPaymentCache(Long expoId) {
        log.info("결제 통계 캐시 완전 삭제 시작 - ExpoId: {}", expoId);

        // 모든 결제 관련 캐시 키 삭제
        String pendingKey = REDIS_KEY_PREFIX + expoId + ":pending_payments:v3";
        String revenueKey = REDIS_KEY_PREFIX + expoId + ":today_revenue:v3";
        String completedKey = REDIS_KEY_PREFIX + expoId + ":completed_payments:v2";
        String refundedKey = REDIS_KEY_PREFIX + expoId + ":refunded_payments:v2";
        String totalRevenueKey = REDIS_KEY_PREFIX + expoId + ":total_revenue:v2";

        redisTemplate.delete(pendingKey);
        redisTemplate.delete(revenueKey);
        redisTemplate.delete(completedKey);
        redisTemplate.delete(refundedKey);
        redisTemplate.delete(totalRevenueKey);

        // 티켓 통계 캐시도 삭제
        ticketStatsService.clearTicketCache(expoId);

        log.info("결제 통계 캐시 완전 삭제 완료 - ExpoId: {}", expoId);
    }

    // === 헬퍼 메서드들 ===

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