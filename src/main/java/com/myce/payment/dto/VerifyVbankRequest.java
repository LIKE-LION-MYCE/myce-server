package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyVbankRequest {
    private String impUid;
    private String merchantUid;
    private Integer amount;
    private PaymentTargetType targetType;
    private Long targetId;
}
