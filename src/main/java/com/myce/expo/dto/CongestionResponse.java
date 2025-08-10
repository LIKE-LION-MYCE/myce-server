package com.myce.expo.dto;

import com.myce.expo.entity.type.CongestionLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CongestionResponse {
    
    private Long expoId;
    private String expoTitle;
    private long hourlyVisitors;        // 현재 입장자 수 (1시간 내)
    private int hourlyCapacity;             // 1시간 내 적정 입장자 수
    private double congestionPercentage; // 혼잡도 비율 (%)
    private CongestionLevel level;       // 혼잡도 레벨
    private String levelDisplayName;     // 혼잡도 레벨 표시명
    private String message;              // 혼잡도 메시지
    private String lastUpdated;          // 마지막 업데이트 시간
    
    public static CongestionResponse of(Long expoId, String expoTitle, 
                                       long hourlyVisitors, int hourlyCapacity) {
        double percentage = hourlyCapacity > 0 ?
            (double) hourlyVisitors / hourlyCapacity * 100 : 0.0;
        
        CongestionLevel level = CongestionLevel.fromPercentage(percentage);
        
        return CongestionResponse.builder()
                .expoId(expoId)
                .expoTitle(expoTitle)
                .hourlyVisitors(hourlyVisitors)
                .hourlyCapacity(hourlyCapacity)
                .congestionPercentage(Math.round(percentage * 100.0) / 100.0) // 소수점 2자리
                .level(level)
                .levelDisplayName(level.getDisplayName())
                .message(level.getMessage())
                .lastUpdated(java.time.LocalDateTime.now().toString())
                .build();
    }
}