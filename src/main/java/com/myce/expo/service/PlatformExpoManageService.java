package com.myce.expo.service;

import com.myce.expo.dto.ExpoPaymentPreviewResponse;

/**
 * 플랫폼 관리자용 박람회 신청 관리 서비스 인터페이스
 */
public interface PlatformExpoManageService {

    /**
     * 박람회 신청 승인
     */
    void approveExpoApplication(Long expoId);

    /**
     * 박람회 신청 거절
     */
    void rejectExpoApplication(Long expoId, String reason);

    /**
     * 박람회 취소 승인
     */
    void approveCancellation(Long expoId);

    /**
     * 박람회 정산 승인
     * SETTLEMENT_REQUESTED -> COMPLETED
     */
    void approveSettlement(Long expoId, Long adminMemberId);
    
    /**
     * 박람회 승인 시 결제 정보 미리보기
     * DB 저장 없이 계산된 결제 정보 반환
     */
    ExpoPaymentPreviewResponse getPaymentPreview(Long expoId);
}