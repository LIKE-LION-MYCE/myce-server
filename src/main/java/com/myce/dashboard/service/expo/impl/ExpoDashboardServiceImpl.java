package com.myce.dashboard.service.expo.impl;

import com.myce.dashboard.dto.expo.*;
import com.myce.dashboard.service.expo.ExpoDashboardService;
import com.myce.dashboard.service.expo.ReservationStatsService;
import com.myce.dashboard.service.expo.CheckinStatsService;
import com.myce.dashboard.service.expo.PaymentStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoDashboardServiceImpl implements ExpoDashboardService {
    
    private final ReservationStatsService reservationStatsService;
    private final CheckinStatsService checkinStatsService;
    private final PaymentStatsService paymentStatsService;
    
    @Override
    public ExpoDashboardResponse getExpoDashboard(Long expoId) {
        return ExpoDashboardResponse.builder()
                .reservationStats(reservationStatsService.getReservationStats(expoId))
                .checkinStats(checkinStatsService.getCheckinStats(expoId))
                .paymentStats(paymentStatsService.getPaymentStats(expoId))
                .expoDisplayDateRange(reservationStatsService.getExpoDisplayDateRange(expoId))
                .build();
    }

    @Override
    public List<DailyReservation> getWeeklyReservationsByDateRange(Long expoId, LocalDate startDate, LocalDate endDate) {
        return reservationStatsService.getWeeklyReservationsByDateRange(expoId, startDate, endDate);
    }

    @Override
    public LocalDate[] getExpoDisplayDateRange(Long expoId) {
        return reservationStatsService.getExpoDisplayDateRange(expoId);
    }

    @Override
    public void refreshReservationCache(Long expoId) {
        reservationStatsService.refreshReservationCache(expoId);
    }

    @Override
    public void refreshCheckinCache(Long expoId) {
        checkinStatsService.refreshCheckinCache(expoId);
    }

    @Override
    public void refreshPaymentCache(Long expoId) {
        paymentStatsService.refreshPaymentCache(expoId);
    }
    
    @Override
    public void clearReservationCache(Long expoId) {
        reservationStatsService.clearReservationCache(expoId);
    }

    @Override
    public void clearCheckinCache(Long expoId) {
        checkinStatsService.clearCheckinCache(expoId);
    }

    @Override
    public void clearPaymentCache(Long expoId) {
        paymentStatsService.clearPaymentCache(expoId);
    }
}