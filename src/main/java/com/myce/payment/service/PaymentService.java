package com.myce.payment.service;

import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import java.util.Map;

public interface PaymentService {
  // 액세스 토큰 가져오기
  String getAccessToken();

  // 결제 검증
  PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request);

  // 결제내역 조회
  Map<String, Object> getPaymentInfo(String impUid, String accessToken);

//  // 결제 환불
//  Map<String, Object> refundPayment(String impUid, int amount, String reason);
}
