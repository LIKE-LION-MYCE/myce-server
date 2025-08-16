package com.myce.qrcode.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.qrcode.dto.QrUseResponse;
import com.myce.qrcode.dto.QrVerifyResponse;

public interface QrCodeService {

    void issueQr(Long reserverId);
    void reissueQr(Long reserverId, Long adminMemberId, LoginType loginType);
    QrUseResponse updateQrAsUsed(String qrToken, Long adminMemberId, LoginType loginType);
    String getQrImageUrlByReserverId(Long reserverId);
    String getQrImageUrlByToken(String token);
    QrVerifyResponse verifyQrCode(String token, Long adminMemberId, LoginType loginType);
}
