package com.myce.qrcode.service;

import java.util.Optional;

public interface QrCodeService {

    void issueQr(Long reserverId);
    void reissueQr(Long reserverId, Long adminMemberId);
    void markQrAsUsed(String qrToken, Long adminMemberId);
    String getQrImageUrlByReserverId(Long reserverId);
    String getQrImageUrlByToken(String token);
}
