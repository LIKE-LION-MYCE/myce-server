package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 플랫폼 관리자용 박람회 거절 사유 응답 DTO
 */
@Builder
@Getter
public class ExpoRejectionInfoResponse {
    
    /**
     * 박람회 ID
     */
    private Long expoId;
    
    /**
     * 거절 사유
     */
    private String reason;
    
    /**
     * 거절 처리 일시
     */
    private LocalDateTime rejectedAt;
}