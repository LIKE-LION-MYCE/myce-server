package com.myce.refund.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.refund.dto.ReservationRefundCalculation;
import com.myce.refund.service.RefundRequestService;
import com.myce.refund.service.ReservationRefundCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundRequestService refundRequestService;
    private final ReservationRefundCalculationService refundCalculationService;

    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<String> refundReservation(
            @PathVariable Long reservationId,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        refundRequestService.createReservationRefund(currentUser.getMemberId(), reservationId, reason);
        
        return ResponseEntity.ok("예매가 성공적으로 환불되었습니다.");
    }

    @GetMapping("/reservation/{reservationId}/preview")
    public ResponseEntity<ReservationRefundCalculation> getRefundPreview(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        ReservationRefundCalculation calculation = refundCalculationService.calculateRefundAmount(reservationId);
        
        return ResponseEntity.ok(calculation);
    }
}