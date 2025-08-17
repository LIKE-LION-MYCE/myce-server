package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ReservationStats {
    // Redis 캐시 데이터
    private Long todayReservations;
    private List<DailyReservation> weeklyReservations;
    
    // RDB 직접 조회 데이터
    private Long totalReservations;
    private GenderStats genderStats;
    private AgeGroupStats ageGroupStats;
    private String dataSource;
    
    @Builder
    public ReservationStats(Long todayReservations, 
                           List<DailyReservation> weeklyReservations,
                           Long totalReservations,
                           GenderStats genderStats,
                           AgeGroupStats ageGroupStats,
                           String dataSource) {
        this.todayReservations = todayReservations;
        this.weeklyReservations = weeklyReservations;
        this.totalReservations = totalReservations;
        this.genderStats = genderStats;
        this.ageGroupStats = ageGroupStats;
        this.dataSource = dataSource != null ? dataSource : "mixed";
    }
}