package com.myce.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdRefundRequest {
    private Long adId;
    private String reason;
    private Integer cancelAmount; // null이면 전액 환불, 값이 있으면 부분 환불
}