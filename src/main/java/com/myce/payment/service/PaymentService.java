package com.myce.payment.service;

import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.entity.type.PaymentStatus;
import java.util.Map;

public interface PaymentService {
  // 결제 검증
  PaymentVerifyResponse verifyPayment(PaymentVerifyInfo request);

  // 결제 환불
  Map<String, Object> refundPayment(PaymentRefundRequest request);

  // 가상계좌 확인 및 PENDING 상태 저장
  PaymentVerifyResponse verifyVbankPayment(PaymentVerifyInfo request);

  // 포트원 웹훅 처리
  void processWebhook(PortOneWebhookRequest request);

  // AdPaymentInfo 상태 변경
  void updateAdPaymentInfo(Long adId, PaymentStatus paymentStatus);
}
