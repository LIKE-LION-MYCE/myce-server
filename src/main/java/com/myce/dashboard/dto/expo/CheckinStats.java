package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class CheckinStats {
    // Redis 캐시 데이터
    private Long reservedTickets;
    private Long qrCheckinSuccess;
    private Float checkinProgress;
    private List<HourlyCheckin> hourlyCheckins;
    private String dataSource;
    
    @Builder
    public CheckinStats(Long reservedTickets,
                       Long qrCheckinSuccess,
                       Float checkinProgress,
                       List<HourlyCheckin> hourlyCheckins,
                       String dataSource) {
        this.reservedTickets = reservedTickets;
        this.qrCheckinSuccess = qrCheckinSuccess;
        this.checkinProgress = checkinProgress;
        this.hourlyCheckins = hourlyCheckins;
        this.dataSource = dataSource != null ? dataSource : "redis";
    }
}