package com.myce.payment.service.mapper;

import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;

public class PaymentMapper {
  // 결제 검증 응답 반환
  public static PaymentVerifyResponse toPaymentVerifyResponse(PaymentVerifyRequest request, String status, Integer paidAmount) {
    return PaymentVerifyResponse.builder()
        .impUid(request.getImpUid())
        .merchantUid(request.getMerchantUid())
        .status(status)
        .amount(paidAmount)
        .build();
  }
}
