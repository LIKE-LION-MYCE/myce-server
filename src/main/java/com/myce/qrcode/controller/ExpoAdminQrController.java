package com.myce.qrcode.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.dto.ExpoAdminQrReissueRequest;
import com.myce.qrcode.service.ExpoAdminQrService;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/reservers")
@RequiredArgsConstructor
public class ExpoAdminQrController {

    private final ExpoAdminQrService service;

    @PutMapping("{reserverId}/manual-checkin")
    public ResponseEntity<ExpoAdminReservationResponse> updateReserverQrCodeForManualCheckIn(
            @PathVariable Long expoId,
            @PathVariable Long reserverId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        return ResponseEntity.ok(service.updateReserverQrCodeForManualCheckIn(expoId,memberId,loginType,reserverId));
    }

    @PostMapping("/qr-reissue")
    public ResponseEntity<List<ExpoAdminReservationResponse>> reissueReserverQrCode(
            @PathVariable Long expoId,
            @RequestBody @Valid ExpoAdminQrReissueRequest dto,
            @RequestParam(required = false) String entranceStatus,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String reservationCode,
            @RequestParam(required = false) String ticketName,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        LoginType loginType = customUserDetails.getLoginType();

        List<ExpoAdminReservationResponse> result = service.reissueReserverQrCode(expoId,
                                                                                  memberId,
                                                                                  loginType,
                                                                                  dto,
                                                                                  entranceStatus,
                                                                                  name,
                                                                                  phone,
                                                                                  reservationCode,
                                                                                  ticketName);

        return ResponseEntity.ok(result);
    }
}