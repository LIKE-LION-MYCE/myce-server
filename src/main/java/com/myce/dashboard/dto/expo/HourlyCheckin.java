package com.myce.dashboard.dto.expo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HourlyCheckin {
    private String timeRange;
    private Long checkinCount;
    
    @Builder
    public HourlyCheckin(String timeRange, Long checkinCount) {
        this.timeRange = timeRange;
        this.checkinCount = checkinCount;
    }
}