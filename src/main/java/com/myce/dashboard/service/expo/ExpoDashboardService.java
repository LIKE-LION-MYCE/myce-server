package com.myce.dashboard.service.expo;

import com.myce.dashboard.dto.expo.ExpoDashboardResponse;
import com.myce.dashboard.dto.expo.DailyReservation;

import java.time.LocalDate;
import java.util.List;

public interface ExpoDashboardService {
    
    ExpoDashboardResponse getExpoDashboard(Long expoId);
    
    void refreshReservationCache(Long expoId);
    
    void refreshCheckinCache(Long expoId);
    
    void refreshPaymentCache(Long expoId);
    
    void clearReservationCache(Long expoId);
    
    void clearCheckinCache(Long expoId);
    
    void clearPaymentCache(Long expoId);
    
    // 박람회 표시 기간 조회
    LocalDate[] getExpoDisplayDateRange(Long expoId);
    
    // 커스텀 날짜 범위로 예약 현황 조회
    List<DailyReservation> getWeeklyReservationsByDateRange(Long expoId, LocalDate startDate, LocalDate endDate);
}