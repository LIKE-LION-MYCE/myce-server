package com.myce.dashboard.service.expo;

import com.myce.dashboard.dto.expo.CheckinStats;
import com.myce.dashboard.dto.expo.HourlyCheckin;

import java.time.LocalDate;
import java.util.List;

public interface CheckinStatsService {
    
    /**
     * 특정 박람회의 체크인 통계를 조회합니다.
     */
    CheckinStats getCheckinStats(Long expoId);
    
    /**
     * 특정 날짜의 시간대별 체크인 통계를 조회합니다.
     */
    List<HourlyCheckin> getHourlyCheckinsByDate(Long expoId, LocalDate date);
    
    /**
     * 체크인 통계 캐시를 갱신합니다.
     */
    void refreshCheckinCache(Long expoId);
    
    /**
     * 체크인 통계 캐시를 완전히 삭제합니다.
     */
    void clearCheckinCache(Long expoId);
}