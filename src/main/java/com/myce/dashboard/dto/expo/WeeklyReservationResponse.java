package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class WeeklyReservationResponse {
    private LocalDate[] expoDisplayDateRange;  // 박람회 게시 기간 범위 [displayStartDate, displayEndDate] 
    private List<DailyReservation> dailyReservations;  // 선택 기간의 일별 예약 데이터
    
    @Builder
    public WeeklyReservationResponse(LocalDate[] expoDisplayDateRange, 
                                   List<DailyReservation> dailyReservations) {
        this.expoDisplayDateRange = expoDisplayDateRange;
        this.dailyReservations = dailyReservations;
    }
}