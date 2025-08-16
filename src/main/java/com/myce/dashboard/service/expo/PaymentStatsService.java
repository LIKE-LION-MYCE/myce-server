package com.myce.dashboard.service.expo;

import com.myce.dashboard.dto.expo.PaymentStats;

public interface PaymentStatsService {
    
    /**
     * 특정 박람회의 결제 통계를 조회합니다.
     */
    PaymentStats getPaymentStats(Long expoId);
    
    /**
     * 결제 통계 캐시를 갱신합니다.
     */
    void refreshPaymentCache(Long expoId);
    
    /**
     * 결제 통계 캐시를 완전히 삭제합니다.
     */
    void clearPaymentCache(Long expoId);
}