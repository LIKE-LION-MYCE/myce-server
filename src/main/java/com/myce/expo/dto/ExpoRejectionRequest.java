package com.myce.expo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 박람회 신청 거절 요청 DTO
 * 
 * @author MYCE Team
 * @since 2025-08-11
 */
@Getter
@NoArgsConstructor
public class ExpoRejectionRequest {
    
    /**
     * 거절 사유
     */
    private String reason;
}