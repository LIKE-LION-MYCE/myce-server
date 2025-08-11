package com.myce.qrcode.service;

import java.util.Map;

public interface QrCodeService {

    void issueQr(Long reserverId);
    void reissueQr(Long reserverId, Long adminMemberId);
    void markQrAsUsed(String qrToken, Long adminMemberId);
    String getQrImageUrlByReserverId(Long reserverId);
    String getQrImageUrlByToken(String token);
    Map<String, Object> verifyQrCode(String token);
}
