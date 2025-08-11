package com.myce.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {
    private String impUid;
    private String merchantUid;
    private Integer cancelAmount; // null이면 전액 환불
    private String reason;
    private String refundHolder;
    private String refundBank;
    private String refundAccount;
    private String refundTel;
}
