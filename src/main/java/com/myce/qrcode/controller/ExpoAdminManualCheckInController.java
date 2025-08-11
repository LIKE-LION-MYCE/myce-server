package com.myce.qrcode.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.service.ExpoAdminManualCheckInService;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expos/{expoId}/reservers/{reserverId}/manual-checkin")
@RequiredArgsConstructor
public class ExpoAdminManualCheckInController {

    private final ExpoAdminManualCheckInService service;

    @PutMapping
    public ResponseEntity<ExpoAdminReservationResponse> updateReserverQrCodeForManualCheckIn(
            @PathVariable Long expoId,
            @PathVariable Long reserverId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        return ResponseEntity.ok(service.updateReserverQrCodeForManualCheckIn(expoId,memberId,loginType,reserverId));
    }
}
