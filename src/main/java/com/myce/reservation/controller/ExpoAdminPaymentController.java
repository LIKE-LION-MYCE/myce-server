package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.service.ExpoAdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/payments")
@RequiredArgsConstructor
public class ExpoAdminPaymentController {

    private final ExpoAdminPaymentService service;

    @GetMapping//TODO:하위관리자
    public ResponseEntity<List<ExpoAdminPaymentResponse>> getExpoAdminPayment(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(service.getMyExpoPayments(expoId,memberId));
    }
}
