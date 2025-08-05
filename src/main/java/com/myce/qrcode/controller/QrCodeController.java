package com.myce.qrcode.controller;

import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qrcodes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/issue/{reserverId}")
    public ResponseEntity<Void> issue(@PathVariable Long reserverId) throws Exception {
        qrCodeService.issueQr(reserverId);
        return ResponseEntity.ok().build(); // 201 Created
    }

    @PostMapping("/reissue/{reserverId}")
    public ResponseEntity<Void> reissue(@PathVariable Long reserverId,
                                        @RequestParam Long adminId) throws Exception {
        qrCodeService.reissueQr(reserverId, adminId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/token/{token}/use")
    public ResponseEntity<Void> useByToken(@PathVariable String token,
                                           @RequestParam Long adminId) {
        qrCodeService.markQrAsUsed(token, adminId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reserver/{reserverId}")
    public ResponseEntity<String> getQrUrlByReserverId(@PathVariable Long reserverId) {
        return qrCodeService.getQrImageUrlByReserverId(reserverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<String> getQrUrlByToken(@PathVariable String token) {
        return qrCodeService.getQrImageUrlByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}


