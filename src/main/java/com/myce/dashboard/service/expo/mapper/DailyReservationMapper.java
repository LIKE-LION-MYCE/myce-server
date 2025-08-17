package com.myce.dashboard.service.expo.mapper;

import com.myce.dashboard.dto.expo.DailyReservation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DailyReservationMapper {

    /**
     * DB 쿼리 결과(Object[])를 DailyReservation 리스트로 변환
     * @param queryResults DB에서 조회한 [날짜, 카운트] 결과
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 완성된 DailyReservation 리스트 (빈 날짜 포함, 정렬됨)
     */
    public List<DailyReservation> mapFromQueryResults(List<Object[]> queryResults, LocalDate startDate, LocalDate endDate) {
        List<DailyReservation> reservations = new ArrayList<>();
        
        // DB 결과를 DailyReservation으로 변환
        for (Object[] result : queryResults) {
            java.sql.Date sqlDate = (java.sql.Date) result[0];
            LocalDate date = sqlDate.toLocalDate();
            Long count = ((Number) result[1]).longValue();
            
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            
            reservations.add(DailyReservation.builder()
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .reservationCount(count)
                    .build());
        }
        
        // 빈 날짜 채우기 (0으로)
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate checkDate = current;
            boolean exists = reservations.stream().anyMatch(r -> r.getDate().equals(checkDate));
            
            if (!exists) {
                String dayOfWeek = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                reservations.add(DailyReservation.builder()
                        .date(current)
                        .dayOfWeek(dayOfWeek)
                        .reservationCount(0L)
                        .build());
            }
            current = current.plusDays(1);
        }
        
        // 날짜 순으로 정렬
        reservations.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        return reservations;
    }

    /**
     * 날짜와 카운트로 DailyReservation 생성 (주간 데이터용)
     */
    public DailyReservation mapFromDateAndCount(LocalDate date, Long count) {
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
        
        return DailyReservation.builder()
                .date(date)
                .dayOfWeek(dayOfWeek)
                .reservationCount(count != null ? count : 0L)
                .build();
    }

    /**
     * 주간 예약 데이터 생성 (최근 7일)
     */
    public List<DailyReservation> createWeeklyReservations(java.util.function.Function<String, Long> cacheValueProvider) {
        List<DailyReservation> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long count = cacheValueProvider.apply("reservations:weekly:" + date.toString());
            result.add(mapFromDateAndCount(date, count));
        }
        
        return result;
    }
}