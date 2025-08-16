package com.myce.qrcode.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.dto.QrTokenRequest;
import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;
import com.myce.qrcode.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qrcodes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/issue/{reserverId}")
    public ResponseEntity<Void> issue(@PathVariable Long reserverId) {
        qrCodeService.issueQr(reserverId);
        return ResponseEntity.ok().build(); // 201 Created
    }

    @PostMapping("/reservation/{reservationId}/generate")
    public ResponseEntity<Void> generateQrForReservation(@PathVariable Long reservationId) {
        try {
            qrCodeService.issueQrForReservation(reservationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // QR 생성 실패 시에도 200 반환 (스케줄러에서 처리되므로)
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/reissue/{reserverId}")
    public ResponseEntity<Void> reissue(@PathVariable Long reserverId,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long adminId = customUserDetails.getMemberId();
        qrCodeService.reissueQr(reserverId, adminId, customUserDetails.getLoginType());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/use")
    public ResponseEntity<QrUseResponse> useQrCode(@RequestBody QrTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long adminId = customUserDetails.getMemberId();
        QrUseResponse response = qrCodeService.updateQrAsUsed(request.getToken(), adminId, customUserDetails.getLoginType());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reserver/{reserverId}")
    public ResponseEntity<String> getQrUrlByReserverId(@PathVariable Long reserverId) {
        String url = qrCodeService.getQrImageUrlByReserverId(reserverId);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<String> getQrUrlByToken(@PathVariable String token) {
        String url = qrCodeService.getQrImageUrlByToken(token);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/verify")
    public ResponseEntity<QrVerifyResponse> verifyQrCode(@RequestBody QrTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long adminId = customUserDetails.getMemberId();
        QrVerifyResponse result = qrCodeService.verifyQrCode(request.getToken(), adminId, customUserDetails.getLoginType());
        return ResponseEntity.ok(result);
    }

}


