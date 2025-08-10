package com.myce.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentInfoForRefund {
  private final Object paymentInfoEntity;
  private final Integer originalPaidAmount;
}
