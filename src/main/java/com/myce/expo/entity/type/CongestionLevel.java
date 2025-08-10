package com.myce.expo.entity.type;

public enum CongestionLevel {
    
    LOW("여유", "입장하기 좋은 시간입니다"),
    MODERATE("보통", "적당한 인원이 방문 중입니다"),
    HIGH("혼잡", "많은 인원이 방문 중입니다"),
    VERY_HIGH("매우혼잡", "입장 대기가 있을 수 있습니다");
    
    private final String displayName;
    private final String message;
    
    CongestionLevel(String displayName, String message) {
        this.displayName = displayName;
        this.message = message;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static CongestionLevel fromPercentage(double percentage) {
        if (percentage <= 25.0) {
            return LOW;
        } else if (percentage <= 50.0) {
            return MODERATE;
        } else if (percentage <= 75.0) {
            return HIGH;
        } else {
            return VERY_HIGH;
        }
    }
}