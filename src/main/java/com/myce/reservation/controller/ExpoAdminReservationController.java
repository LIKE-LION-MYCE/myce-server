package com.myce.reservation.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.service.ExpoAdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/reservations")
@RequiredArgsConstructor
public class ExpoAdminReservationController {

    private final ExpoAdminReservationService service;

    @GetMapping("/ticket-name")
    public ResponseEntity<List<String>> getExpoTicketNames(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        return ResponseEntity.ok(service.getExpoTicketNames(expoId,memberId,loginType));
    }

    @GetMapping
    public ResponseEntity<PagedModel<ExpoAdminReservationResponse>> getMyExpoReservations(
            @PathVariable Long expoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String entranceStatus,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String reservationCode,
            @RequestParam(required = false) String ticketName,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        Pageable pageable = PageRequest.of(page, size);
        Page<ExpoAdminReservationResponse> result = service.getMyExpoReservations(
                expoId, memberId, loginType,
                entranceStatus, name, phone, reservationCode, ticketName,
                pageable);

        return ResponseEntity.ok(new PagedModel<>(result));
    }
}