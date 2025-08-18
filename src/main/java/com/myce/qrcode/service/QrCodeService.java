package com.myce.qrcode.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;

public interface QrCodeService {

    void issueQr(Long reserverId);
    void issueQrWithoutNotification(Long reserverId); // 알림 없이 QR 생성 (스케줄러용)
    void reissueQr(Long reserverId, Long adminMemberId, LoginType loginType);
    QrUseResponse updateQrAsUsed(String qrToken, Long adminMemberId, LoginType loginType);
    String getQrImageUrlByReserverId(Long reserverId);
    String getQrImageUrlByToken(String token);
    QrVerifyResponse verifyQrCode(String token, Long adminMemberId, LoginType loginType);
    void issueQrForReservation(Long reservationId);
}
