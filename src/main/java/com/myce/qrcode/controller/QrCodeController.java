package com.myce.qrcode.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qrcodes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/issue/{reserverId}")
    public ResponseEntity<QrCode> issue(@PathVariable Long reserverId) throws Exception {
        return ResponseEntity.ok(qrCodeService.issueQr(reserverId));
    }

    @PostMapping("/reissue/{reserverId}")
    public ResponseEntity<QrCode> reissue(@PathVariable Long reserverId,
                                          @RequestParam Long adminId) throws Exception {
        return ResponseEntity.ok(qrCodeService.reissueQr(reserverId, adminId));
    }

    @PostMapping("/token/{token}/use")
    public ResponseEntity<Void> useByToken(@PathVariable String token,
                                           @RequestParam Long adminId) {
        qrCodeService.markQrAsUsed(token, adminId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reserver/{reserverId}")
    public ResponseEntity<QrCode> getByReserverId(@PathVariable Long reserverId) {
        return qrCodeService.getQrByReserverId(reserverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<QrCode> getByToken(@PathVariable String token) {
        return qrCodeService.getQrByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}


