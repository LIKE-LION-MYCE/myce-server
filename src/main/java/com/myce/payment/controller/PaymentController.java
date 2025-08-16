package com.myce.payment.controller;

import com.myce.payment.dto.AdPaymentInfoStatusUpdateRequest;
import com.myce.payment.dto.PaymentImpUidForRefundRequest;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.service.PaymentService;
import com.myce.payment.service.refund.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
  private final PaymentService paymentService;
  private final PaymentRefundService paymentRefundService;

  // 결제 검증 API (POST 방식)
  @PostMapping("/verify")
  public ResponseEntity<PaymentVerifyResponse> verifyPayment(
      @RequestBody PaymentVerifyRequest request) {
    PaymentVerifyResponse response = paymentService.verifyPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // 결제 환불 API (POST 방식)
  @PostMapping("/refund")
  public ResponseEntity<Map<String, Object>> refundPayment(
      @RequestBody PaymentRefundRequest request) {
    Map<String, Object> response = paymentService.refundPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // 가상계좌 확인 및 PENDING 상태 저장 API
  @PostMapping("/verify-vbank")
  public ResponseEntity<PaymentVerifyResponse> verifyVbankPayment(
      @RequestBody PaymentVerifyRequest request) {
    PaymentVerifyResponse response = paymentService.verifyVbankPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // 포트원 웹훅 API
  @PostMapping("/webhook")
  public ResponseEntity<Void> portoneWebhook(@RequestBody PortOneWebhookRequest request) {
    log.info("[포트원 웹훅]: {}", request);
    paymentService.processWebhook(request);
    return ResponseEntity.ok().build();
  }

  // 프론트에서 target_id, target_type을 통해 imp_uid 가져오기
  @PostMapping("/imp-uid")
  public ResponseEntity<String> impUid(
      @RequestBody PaymentImpUidForRefundRequest request
  ) {
    String impUid = paymentRefundService.getImpUidForRefund(request);
    return ResponseEntity.ok(impUid);
  }

  @PatchMapping("/{adId}/status")
  public ResponseEntity<Void> updateAdPaymentInfoStatus(
      @PathVariable Long adId, @RequestBody AdPaymentInfoStatusUpdateRequest request
  ){
    paymentService.updateAdPaymentInfo(adId, request.getPaymentStatus());
    return ResponseEntity.ok().build();
  }
}
