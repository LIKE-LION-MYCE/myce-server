package com.myce.refund.service;

import com.myce.refund.dto.ReservationRefundCalculation;

public interface ReservationRefundCalculationService {
    
    // 예매 환불 금액 계산
    ReservationRefundCalculation calculateRefundAmount(Long reservationId);
}