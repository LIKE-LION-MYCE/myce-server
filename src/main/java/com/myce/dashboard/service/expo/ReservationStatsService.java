package com.myce.dashboard.service.expo;

import com.myce.dashboard.dto.expo.ReservationStats;
import com.myce.dashboard.dto.expo.DailyReservation;
import java.time.LocalDate;
import java.util.List;

public interface ReservationStatsService {
    
    /**
     * 특정 박람회의 예약 통계를 조회합니다.
     */
    ReservationStats getReservationStats(Long expoId);
    
    /**
     * 특정 날짜 범위의 일별 예약 통계를 조회합니다.
     */
    List<DailyReservation> getWeeklyReservationsByDateRange(Long expoId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 예약 통계 캐시를 갱신합니다.
     */
    void refreshReservationCache(Long expoId);
    
    /**
     * 예약 통계 캐시를 완전히 삭제합니다.
     */
    void clearReservationCache(Long expoId);
    
    /**
     * 박람회 전시 기간 범위를 조회합니다.
     */
    LocalDate[] getExpoDisplayDateRange(Long expoId);
}