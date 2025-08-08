package com.myce.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentVerifyRequest {
  private String impUid;
  private String merchantUid;
  private Integer amount;
}
