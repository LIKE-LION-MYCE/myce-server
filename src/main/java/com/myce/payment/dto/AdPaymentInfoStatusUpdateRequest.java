package com.myce.payment.dto;

import com.myce.payment.entity.type.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdPaymentInfoStatusUpdateRequest {
  private PaymentStatus  paymentStatus;
}
