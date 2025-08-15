package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.*;
import com.myce.dashboard.service.expo.ExpoDashboardService;
import com.myce.member.entity.type.Gender;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoDashboardServiceImpl implements ExpoDashboardService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final QrCodeRepository qrCodeRepository;
    private final ReservationPaymentInfoRepository paymentInfoRepository;
    private final ExpoRepository expoRepository;
    
    private static final String REDIS_KEY_PREFIX = "expo:stats:";
    private static final int CACHE_TTL_MINUTES = 10;
    private static final int HEAVY_CACHE_TTL_MINUTES = 30; // 무거운 쿼리용 긴 캐시
    
    @Override
    public ExpoDashboardResponse getExpoDashboard(Long expoId) {
        return ExpoDashboardResponse.builder()
                .reservationStats(getReservationStats(expoId))
                .checkinStats(getCheckinStats(expoId))
                .paymentStats(getPaymentStats(expoId))
                .build();
    }
    
    private ReservationStats getReservationStats(Long expoId) {
        // Redis에서 캐시된 데이터 조회 (실시간성 중요)
        String cacheKey = expoId + ":reservations:today";
        Long todayReservations = getCachedValue(cacheKey, Long.class);
        if (todayReservations == null) {
            todayReservations = reservationRepository.countTodayReservationsByExpoId(expoId, LocalDate.now());
        }
        
        List<DailyReservation> weeklyReservations = getWeeklyReservations(expoId);
        
        // RDB에서 직접 조회할 데이터 (정확성 중요) - 캐싱 적용
        Long totalReservations = getCachedValueOrCompute(
            expoId + ":total_reservations",
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
    
    private CheckinStats getCheckinStats(Long expoId) {
        // Redis에서 캐시된 데이터 조회 (실시간성 중요)
        String reservedCacheKey = expoId + ":checkin:reserved";
        String successCacheKey = expoId + ":checkin:success";
        
        Long reservedTickets = getCachedValue(reservedCacheKey, Long.class);
        if (reservedTickets == null) {
            reservedTickets = qrCodeRepository.countReservedTicketsByExpoId(expoId);
        }
        
        Long qrCheckinSuccess = getCachedValue(successCacheKey, Long.class);
        if (qrCheckinSuccess == null) {
            qrCheckinSuccess = qrCodeRepository.countSuccessfulCheckinsByExpoId(expoId);
        }
        
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
    
    private PaymentStats getPaymentStats(Long expoId) {
        // Redis에서 캐시된 데이터 조회 (실시간성 중요)
        String pendingCacheKey = expoId + ":payment:pending";
        String todayRevenueCacheKey = expoId + ":payment:today_revenue";
        
        Long pendingPayments = getCachedValue(pendingCacheKey, Long.class);
        if (pendingPayments == null) {
            pendingPayments = paymentInfoRepository.countPendingPaymentsByExpoId(expoId);
        }
        
        BigDecimal todayRevenue = getCachedValue(todayRevenueCacheKey, BigDecimal.class);
        if (todayRevenue == null) {
            todayRevenue = paymentInfoRepository.sumTodayRevenueByExpoId(expoId, LocalDate.now());
        }
        
        // RDB 직접 조회 데이터 (정확성 중요) - 캐싱 적용
        Long completedPayments = getCachedValueOrCompute(
            expoId + ":completed_payments",
            () -> paymentInfoRepository.countCompletedPaymentsByExpoId(expoId),
            Long.class,
            HEAVY_CACHE_TTL_MINUTES
        );
        
        Long canceledPayments = getCachedValueOrCompute(
            expoId + ":canceled_payments", 
            () -> paymentInfoRepository.countCancelledPaymentsByExpoId(expoId),
            Long.class,
            HEAVY_CACHE_TTL_MINUTES
        );
        
        Long refundedPayments = getCachedValueOrCompute(
            expoId + ":refunded_payments",
            () -> paymentInfoRepository.countRefundedPaymentsByExpoId(expoId),
            Long.class,
            HEAVY_CACHE_TTL_MINUTES
        );
        
        BigDecimal totalRevenue = getCachedValueOrCompute(
            expoId + ":total_revenue",
            () -> paymentInfoRepository.sumTotalRevenueByExpoId(expoId),
            BigDecimal.class,
            HEAVY_CACHE_TTL_MINUTES
        );
        
        List<TicketSales> ticketSalesDetail = getCachedValueOrCompute(
            expoId + ":ticket_sales",
            () -> getTicketSalesDetail(expoId),
            List.class,
            HEAVY_CACHE_TTL_MINUTES
        );
        
        return PaymentStats.builder()
                .pendingPayments(pendingPayments)
                .todayRevenue(todayRevenue)
                .completedPayments(completedPayments)
                .canceledPayments(canceledPayments)
                .refundedPayments(refundedPayments)
                .totalRevenue(totalRevenue)
                .ticketSalesDetail(ticketSalesDetail)
                .dataSource("mixed")
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getCachedValue(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
            if (value == null) {
                return null;
            }
            
            // 숫자 타입 변환 처리
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
    
    private List<DailyReservation> getCachedWeeklyReservations() {
        List<DailyReservation> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            
            Long count = getCachedValue("reservations:daily:" + date.toString(), Long.class);
            result.add(DailyReservation.builder()
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .reservationCount(count != null ? count : (long) (Math.random() * 100 + 50))
                    .build());
        }
        
        return result;
    }
    
    private List<HourlyCheckin> getCachedHourlyCheckins() {
        List<HourlyCheckin> result = new ArrayList<>();
        
        for (int hour = 9; hour <= 18; hour++) {
            String timeRange = String.format("%02d:00", hour);
            Long count = getCachedValue("checkin:hourly:" + hour, Long.class);
            
            result.add(HourlyCheckin.builder()
                    .timeRange(timeRange)
                    .checkinCount(count != null ? count : (long) (Math.random() * 50 + 10))
                    .build());
        }
        
        return result;
    }
    
    @Override
    public void refreshReservationCache(Long expoId) {
        log.info("예약 통계 캐시 갱신 시작 - ExpoId: {}", expoId);
        
        // 기존 실시간 데이터 갱신
        Long todayCount = reservationRepository.countTodayReservationsByExpoId(expoId, LocalDate.now());
        String todayKey = expoId + ":reservations:today";
        redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + todayKey, 
                todayCount, 
                CACHE_TTL_MINUTES, 
                TimeUnit.MINUTES
        );
        
        // 무거운 쿼리 캐시 갱신
        refreshHeavyReservationCache(expoId);
        
        log.info("예약 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    private void refreshHeavyReservationCache(Long expoId) {
        // 누적 예약자수 캐시 갱신
        Long totalReservations = reservationRepository.countTotalReservationsByExpoId(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":total_reservations",
            totalReservations,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        // 성별 통계 캐시 갱신
        GenderStats genderStats = getGenderStats(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":gender_stats",
            genderStats,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        // 연령대 통계 캐시 갱신
        AgeGroupStats ageStats = getAgeGroupStats(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":age_stats",
            ageStats,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }
    
    @Override
    public void refreshCheckinCache(Long expoId) {
        log.info("체크인 통계 캐시 갱신 시작 - ExpoId: {}", expoId);
        
        // 예약 티켓 수
        Long reservedTickets = qrCodeRepository.countReservedTicketsByExpoId(expoId);
        String reservedCacheKey = expoId + ":checkin:reserved";
        redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + reservedCacheKey,
                reservedTickets,
                CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        
        // QR 체크인 성공 건수
        Long successCount = qrCodeRepository.countSuccessfulCheckinsByExpoId(expoId);
        String successCacheKey = expoId + ":checkin:success";
        redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + successCacheKey,
                successCount,
                CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        
        log.info("체크인 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    @Override
    public void refreshPaymentCache(Long expoId) {
        log.info("결제 통계 캐시 갱신 시작 - ExpoId: {}", expoId);
        
        // 기존 실시간 데이터 갱신
        Long pendingCount = paymentInfoRepository.countPendingPaymentsByExpoId(expoId);
        String pendingCacheKey = expoId + ":payment:pending";
        redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + pendingCacheKey,
                pendingCount,
                CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        
        BigDecimal todayRevenue = paymentInfoRepository.sumTodayRevenueByExpoId(expoId, LocalDate.now());
        String todayRevenueCacheKey = expoId + ":payment:today_revenue";
        redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + todayRevenueCacheKey,
                todayRevenue,
                CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        
        // 무거운 쿼리 캐시 갱신
        refreshHeavyPaymentCache(expoId);
        
        log.info("결제 통계 캐시 갱신 완료 - ExpoId: {}", expoId);
    }
    
    private void refreshHeavyPaymentCache(Long expoId) {
        // 결제 완료/취소/환불 건수 캐시 갱신
        Long completedPayments = paymentInfoRepository.countCompletedPaymentsByExpoId(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":completed_payments",
            completedPayments,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        Long canceledPayments = paymentInfoRepository.countCancelledPaymentsByExpoId(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":canceled_payments",
            canceledPayments,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        Long refundedPayments = paymentInfoRepository.countRefundedPaymentsByExpoId(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":refunded_payments",
            refundedPayments,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        // 총 수익 캐시 갱신
        BigDecimal totalRevenue = paymentInfoRepository.sumTotalRevenueByExpoId(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":total_revenue",
            totalRevenue,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
        
        // 티켓 판매 상세 캐시 갱신
        List<TicketSales> ticketSales = getTicketSalesDetail(expoId);
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + expoId + ":ticket_sales",
            ticketSales,
            HEAVY_CACHE_TTL_MINUTES,
            TimeUnit.MINUTES
        );
    }
    
    // === 헬퍼 메서드들 ===
    
    private GenderStats getGenderStats(Long expoId) {
        List<Object[]> genderResults = reserverRepository.countReserversByGender(expoId);
        
        Long maleCount = 0L;
        Long femaleCount = 0L;
        
        for (Object[] result : genderResults) {
            Gender gender = (Gender) result[0];
            Long count = ((Number) result[1]).longValue();
            
            if (gender == Gender.MALE) {
                maleCount = count;
            } else if (gender == Gender.FEMALE) {
                femaleCount = count;
            }
        }
        
        Long totalCount = maleCount + femaleCount;
        Float malePercentage = totalCount > 0 ? (float) maleCount / totalCount * 100 : 0f;
        Float femalePercentage = totalCount > 0 ? (float) femaleCount / totalCount * 100 : 0f;
        
        return GenderStats.builder()
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .malePercentage(malePercentage)
                .femalePercentage(femalePercentage)
                .build();
    }
    
    private AgeGroupStats getAgeGroupStats(Long expoId) {
        List<Object[]> ageResults = reserverRepository.countReserversByAgeGroup(expoId);
        
        List<AgeGroupStats.AgeGroup> ageGroups = new ArrayList<>();
        Long totalCount = ageResults.stream()
                .mapToLong(result -> ((Number) result[1]).longValue())
                .sum();
        
        for (Object[] result : ageResults) {
            String ageRange = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            Float percentage = totalCount > 0 ? 
                (float) count / totalCount * 100 : 0f;
            
            ageGroups.add(AgeGroupStats.AgeGroup.builder()
                    .ageRange(ageRange)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        
        return AgeGroupStats.builder()
                .ageGroups(ageGroups)
                .build();
    }
    
    private List<DailyReservation> getWeeklyReservations(Long expoId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<Object[]> results = reservationRepository.countReservationsByDateRange(
                expoId, 
                startDate.atStartOfDay(), 
                endDate.plusDays(1).atStartOfDay()
        );
        
        List<DailyReservation> weeklyReservations = new ArrayList<>();
        
        // 7일간의 데이터를 순서대로 정렬
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            
            Long count = results.stream()
                    .filter(result -> {
                        Object dbDate = result[0];
                        // MySQL DATE() 함수 결과를 LocalDate로 변환하여 비교
                        if (dbDate instanceof java.sql.Date) {
                            return ((java.sql.Date) dbDate).toLocalDate().equals(date);
                        } else if (dbDate instanceof LocalDate) {
                            return dbDate.equals(date);
                        }
                        return false;
                    })
                    .findFirst()
                    .map(result -> ((Number) result[1]).longValue())
                    .orElse(0L);
            
            weeklyReservations.add(DailyReservation.builder()
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .reservationCount(count)
                    .build());
        }
        
        return weeklyReservations;
    }
    
    private List<HourlyCheckin> getHourlyCheckins(Long expoId) {
        List<Object[]> results = qrCodeRepository.countHourlyCheckinsByExpoIdAndDate(expoId, LocalDate.now());
        
        List<HourlyCheckin> hourlyCheckins = new ArrayList<>();
        
        // 9시부터 18시까지 시간대별 데이터
        for (int hour = 9; hour <= 18; hour++) {
            final int currentHour = hour; // final 변수로 복사
            String timeRange = String.format("%02d:00", hour);
            
            Long count = results.stream()
                    .filter(result -> ((Number) result[0]).intValue() == currentHour)
                    .findFirst()
                    .map(result -> ((Number) result[1]).longValue())
                    .orElse(0L);
            
            hourlyCheckins.add(HourlyCheckin.builder()
                    .timeRange(timeRange)
                    .checkinCount(count)
                    .build());
        }
        
        return hourlyCheckins;
    }
    
    private List<TicketSales> getTicketSalesDetail(Long expoId) {
        List<Object[]> results = paymentInfoRepository.getTicketSalesDetailByExpoId(expoId);
        
        List<TicketSales> ticketSalesDetail = new ArrayList<>();
        
        for (Object[] result : results) {
            String ticketType = (String) result[0];
            Long soldCount = ((Number) result[1]).longValue();
            BigDecimal avgPrice = result[2] != null ? 
                new BigDecimal(result[2].toString()).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal totalRevenue = result[3] != null ? 
                new BigDecimal(result[3].toString()) : BigDecimal.ZERO;
            
            ticketSalesDetail.add(TicketSales.builder()
                    .ticketType(ticketType)
                    .soldCount(soldCount)
                    .unitPrice(avgPrice)
                    .totalRevenue(totalRevenue)
                    .build());
        }
        
        return ticketSalesDetail;
    }
    
    @Override
    public LocalDate[] getExpoDisplayDateRange(Long expoId) {
        return expoRepository.findById(expoId)
                .map(expo -> new LocalDate[]{expo.getDisplayStartDate(), expo.getDisplayEndDate()})
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 박람회입니다: " + expoId));
    }
    
    @Override
    public List<DailyReservation> getWeeklyReservationsByDateRange(Long expoId, LocalDate startDate, LocalDate endDate) {
        // 날짜 범위 검증
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
        
        // 박람회 표시 기간 내에 있는지 검증
        LocalDate[] displayRange = getExpoDisplayDateRange(expoId);
        if (startDate.isBefore(displayRange[0]) || endDate.isAfter(displayRange[1])) {
            throw new IllegalArgumentException("선택한 날짜가 박람회 표시 기간을 벗어났습니다.");
        }
        
        List<Object[]> results = reservationRepository.countReservationsByDateRange(
                expoId, 
                startDate.atStartOfDay(), 
                endDate.plusDays(1).atStartOfDay()
        );
        
        List<DailyReservation> weeklyReservations = new ArrayList<>();
        
        // 선택된 기간의 데이터를 순서대로 정렬
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            final LocalDate dateToFind = currentDate;
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            
            Long count = results.stream()
                    .filter(result -> {
                        Object dbDate = result[0];
                        // MySQL DATE() 함수 결과를 LocalDate로 변환하여 비교
                        if (dbDate instanceof java.sql.Date) {
                            return ((java.sql.Date) dbDate).toLocalDate().equals(dateToFind);
                        } else if (dbDate instanceof LocalDate) {
                            return dbDate.equals(dateToFind);
                        }
                        return false;
                    })
                    .findFirst()
                    .map(result -> ((Number) result[1]).longValue())
                    .orElse(0L);
            
            weeklyReservations.add(DailyReservation.builder()
                    .date(currentDate)
                    .dayOfWeek(dayOfWeek)
                    .reservationCount(count)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return weeklyReservations;
    }
}