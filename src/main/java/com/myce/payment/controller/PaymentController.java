package com.myce.payment.controller;

import com.myce.payment.dto.AdPaymentInfoStatusUpdateRequest;
import com.myce.payment.dto.PaymentImpUidForRefundRequest;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.PaymentRefundRequest;
import com.myce.payment.dto.PortOneWebhookRequest;
import com.myce.payment.dto.AdRefundRequest;
import com.myce.payment.dto.ReservationPaymentVerifyRequest;
import com.myce.payment.service.PaymentService;
import com.myce.payment.service.ReservationPaymentService;
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
  private final ReservationPaymentService reservationPaymentService;

  // 결제 검증 API (POST 방식)
  @PostMapping("/verify")
  public ResponseEntity<PaymentVerifyResponse> verifyPayment(
      @RequestBody PaymentVerifyInfo request) {
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
      @RequestBody PaymentVerifyInfo request) {
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

  // 광고 통합 환불 API - 포트원 환불 + 광고 상태 변경 + 결제 상태 변경을 한번에 처리
  @PostMapping("/ad-refund")
  public ResponseEntity<Map<String, Object>> processAdRefund(
      @RequestBody AdRefundRequest request) {
    Map<String, Object> response = paymentRefundService.processAdRefund(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
  
  // 박람회 통합 결제 검증 API - 결제 검증 + 예약 처리 + 마일리지 + 등급 업데이트를 한번에 처리
  @PostMapping("/reservation/verify")
  public ResponseEntity<PaymentVerifyResponse> verifyReservationPayment(
      @RequestBody ReservationPaymentVerifyRequest request) {
    log.info("박람회 통합 결제 검증 시작 - reservationId: {}", request.getTargetId());
    PaymentVerifyResponse response = reservationPaymentService.verifyAndCompleteReservationPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
  
  // 박람회 가상계좌 통합 결제 검증 API
  @PostMapping("/reservation/verify-vbank")
  public ResponseEntity<PaymentVerifyResponse> verifyReservationVbankPayment(
      @RequestBody ReservationPaymentVerifyRequest request) {
    log.info("박람회 가상계좌 통합 결제 검증 시작 - reservationId: {}", request.getTargetId());
    PaymentVerifyResponse response = reservationPaymentService.verifyAndPendingVbankReservationPayment(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
