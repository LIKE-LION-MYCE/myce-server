package com.myce.payment.controller;

import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
  private final PaymentService paymentService;

  // 결제 검증 API (POST 방식)
  @PostMapping("/verify")
  public ResponseEntity<PaymentVerifyResponse> verifyPayment(
      @RequestBody PaymentVerifyRequest request) {
    PaymentVerifyResponse response = paymentService.verifyPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // getAccessToken() 임시 테스트용 엔드포인트
  @GetMapping("/test/token")
  public String testGetToken() {
    return paymentService.getAccessToken();
  }

  // getPaymentInfo 임시 테스트용 엔드포인트
  @GetMapping("/test/payment-info/{imp_uid}")
  public java.util.Map<String, Object> testGetPaymentInfo(@PathVariable("imp_uid") String impUid) {
    // 테스트를 위해 Access Token을 직접 발급받습니다.
    String accessToken = paymentService.getAccessToken();
    return paymentService.getPaymentInfo(impUid, accessToken);
  }
}
