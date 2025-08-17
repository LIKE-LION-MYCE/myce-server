package com.myce.refund.service;

import com.myce.refund.dto.RefundRequestDto;

public interface RefundRequestService {
    
    // 박람회 환불 신청
    void createRefundRequest(Long memberId, Long expoId, RefundRequestDto requestDto);
    
    // 개인 예매 환불 (즉시 처리)
    void createReservationRefund(Long memberId, Long reservationId, String reason);
    
}