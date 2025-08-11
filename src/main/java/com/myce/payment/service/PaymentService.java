package com.myce.payment.service;

import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PortOneWebhookRequest;
import java.util.Map;

public interface PaymentService {
  // 결제 검증
  PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);

  // 결제 환불
  Map<String, Object> refundPayment(PaymentRefundRequest request);

  // 가상계좌 확인 및 PENDING 상태 저장
  PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request);

//  // 포트원 웹훅 처리
//  void processWebhook(PortOneWebhookRequest request);

}
