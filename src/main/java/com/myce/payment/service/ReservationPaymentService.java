package com.myce.payment.service;

import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.ReservationPaymentVerifyRequest;

public interface ReservationPaymentService {
    
    /**
     * 박람회 예약 결제 검증 및 통합 처리
     * - 결제 검증
     * - 예약 상태 변경 (PENDING -> CONFIRMED)
     * - 예약자 정보 저장
     * - 티켓 수량 감소
     * - 마일리지 처리 (사용/적립)
     * - 회원 등급 업데이트
     * - QR 코드 생성
     * 모든 작업을 하나의 트랜잭션으로 처리
     */
    PaymentVerifyResponse verifyAndCompleteReservationPayment(ReservationPaymentVerifyRequest request);
    
    /**
     * 가상계좌 박람회 예약 결제 검증 및 처리
     */
    PaymentVerifyResponse verifyAndPendingVbankReservationPayment(ReservationPaymentVerifyRequest request);
}