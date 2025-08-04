package com.myce.qrcode.service;

import com.myce.qrcode.entity.QrCode;

public interface QrCodeService {

    QrCode issueQr(Long reserverId) throws Exception;
    QrCode reissueQr(Long reserverId, Long adminMemberId) throws Exception;
    void markQrAsUsed(String qrToken, Long adminMemberId);
}
