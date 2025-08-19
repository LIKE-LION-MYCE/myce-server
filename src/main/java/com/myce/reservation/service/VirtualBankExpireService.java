package com.myce.reservation.service;

public interface VirtualBankExpireService {
    
    /**
     * 만료된 가상계좌 예약들을 처리
     * - CONFIRMED_PENDING 상태의 예약을 CANCELLED로 변경
     * - 관련 결제 정보를 FAILED로 변경  
     * - 티켓 수량 복구
     */
    void processExpiredVirtualBankReservations();
}