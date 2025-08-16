package com.myce.settlement.service;

/**
 * Settlement 플랫폼 관리자 서비스 인터페이스
 */
public interface SettlementPlatformAdminService {
    
    /**
     * Settlement 정산 승인 (플랫폼 관리자용)
     */
    void approveSettlement(Long expoId, Long adminMemberId);
}