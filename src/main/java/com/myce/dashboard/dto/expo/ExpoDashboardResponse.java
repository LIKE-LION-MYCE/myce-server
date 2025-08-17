package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ExpoDashboardResponse {
    private ReservationStats reservationStats;
    private CheckinStats checkinStats;
    private PaymentStats paymentStats;
    private LocalDate[] expoDisplayDateRange;  // 박람회 게시 기간 범위 [displayStartDate, displayEndDate]
    
    @Builder
    public ExpoDashboardResponse(ReservationStats reservationStats, 
                                CheckinStats checkinStats, 
                                PaymentStats paymentStats,
                                LocalDate[] expoDisplayDateRange) {
        this.reservationStats = reservationStats;
        this.checkinStats = checkinStats;
        this.paymentStats = paymentStats;
        this.expoDisplayDateRange = expoDisplayDateRange;
    }
}