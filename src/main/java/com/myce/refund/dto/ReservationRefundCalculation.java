package com.myce.refund.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationRefundCalculation {
    
    private final Integer originalAmount;      // 원본 결제 금액
    private final Integer refundFee;          // 환불 수수료
    private final Integer actualRefundAmount; // 실제 환불 금액
    private final Integer restoreMileage;     // 복원할 마일리지 (사용한 것)
    private final Integer deductMileage;      // 차감할 마일리지 (적립된 것)
    private final String feeDescription;     // 수수료 산정 근거
}