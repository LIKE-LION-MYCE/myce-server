package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExpoLocationResponse {
    private Long expoId;
    private String expoTitle;
    
    // 위치 정보
    private String location;
    private String locationDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
}