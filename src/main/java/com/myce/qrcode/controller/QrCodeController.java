package com.myce.qrcode.controller;

import com.myce.qrcode.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/reissue/{reserverId}")
    public ResponseEntity<Void> reissue(@PathVariable Long reserverId,
            @RequestParam Long adminId) {
        qrCodeService.reissueQr(reserverId, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/{token}/use")
    public ResponseEntity<Map<String, Object>> useByToken(@PathVariable String token,
            @RequestParam Long adminId) {
        try {
            qrCodeService.markQrAsUsed(token, adminId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "QR 코드가 성공적으로 사용 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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

    @PostMapping("/token/{token}/verify")
    public ResponseEntity<Map<String, Object>> verifyQrCode(@PathVariable String token) {
        try {
            Map<String, Object> result = qrCodeService.verifyQrCode(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}


