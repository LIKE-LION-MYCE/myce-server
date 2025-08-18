package com.myce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerifyResponse {
  private String impUid;
  private String merchantUid;
  private String status;
  private Integer amount;
  private Long reservationId; // 실제 DB에 저장된 reservation ID
}
