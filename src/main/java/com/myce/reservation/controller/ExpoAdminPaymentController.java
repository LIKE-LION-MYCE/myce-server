package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminPaymentDetailResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.service.ExpoAdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/payments")
@RequiredArgsConstructor
public class ExpoAdminPaymentController {

    private final ExpoAdminPaymentService service;

    @GetMapping
    public ResponseEntity<PagedModel<ExpoAdminPaymentResponse>> getMyExpoPayments(
            @PathVariable Long expoId,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<ExpoAdminPaymentResponse> result = service.getMyExpoPayments(expoId, memberId, loginType, status, name, phone, pageable);

        return ResponseEntity.ok(new PagedModel<>(result));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<List<ExpoAdminPaymentDetailResponse>> getPaymentDetail(
            @PathVariable Long expoId,
            @PathVariable Long paymentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId =  customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        return ResponseEntity.ok(service.getPaymentDetail(expoId,memberId,loginType,paymentId));
    }
}
