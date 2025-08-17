package com.myce.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myce.payment.entity.type.PaymentTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentImpUidForRefundRequest {
  @JsonProperty("paymentTargetType")
  private PaymentTargetType targetType;
  private Long targetId;
}
