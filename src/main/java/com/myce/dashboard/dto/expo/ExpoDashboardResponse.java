package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpoDashboardResponse {
    private ReservationStats reservationStats;
    private CheckinStats checkinStats;
    private PaymentStats paymentStats;
    
    @Builder
    public ExpoDashboardResponse(ReservationStats reservationStats, 
                                CheckinStats checkinStats, 
                                PaymentStats paymentStats) {
        this.reservationStats = reservationStats;
        this.checkinStats = checkinStats;
        this.paymentStats = paymentStats;
    }
}