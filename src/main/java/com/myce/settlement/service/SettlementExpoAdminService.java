package com.myce.settlement.service;

import com.myce.member.dto.expo.ExpoSettlementRequest;

/**
 * Settlement 박람회 관리자 서비스 인터페이스
 */
public interface SettlementExpoAdminService {
    
    /**
     * Settlement 정산 신청 (박람회 관리자용)
     */
    void requestSettlement(Long expoId, ExpoSettlementRequest request);
}