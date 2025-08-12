package com.myce.qrcode.service;

import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;

public interface QrCodeService {

    void issueQr(Long reserverId);
    void reissueQr(Long reserverId, Long adminMemberId);
    QrUseResponse markQrAsUsed(String qrToken, Long adminMemberId);
    String getQrImageUrlByReserverId(Long reserverId);
    String getQrImageUrlByToken(String token);
    QrVerifyResponse verifyQrCode(String token);
}
