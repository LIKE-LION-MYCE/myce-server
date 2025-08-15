package com.myce.settlement.service;

import com.myce.expo.entity.Expo;

/**
 * Settlement 시스템/스케줄러 서비스 인터페이스
 */
public interface SettlementSystemService {
    
    /**
     * Settlement 초기 생성 (스케줄러용)
     * 박람회가 PUBLISHED → PUBLISH_ENDED로 전환될 때 자동 호출
     */
    void createInitialSettlement(Expo expo);
}