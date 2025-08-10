package com.myce.payment.service;

import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import java.util.Map;

public interface PaymentService {
  // 결제 검증
  PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);

  // 결제 환불
  Map<String, Object> refundPayment(PaymentRefundRequest request);
}
