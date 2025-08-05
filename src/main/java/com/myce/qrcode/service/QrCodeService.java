package com.myce.qrcode.service;

import java.util.Optional;

public interface QrCodeService {

    void issueQr(Long reserverId) throws Exception;
    void reissueQr(Long reserverId, Long adminMemberId) throws Exception;
    void markQrAsUsed(String qrToken, Long adminMemberId);
    Optional<String> getQrImageUrlByReserverId(Long reserverId);
    Optional<String> getQrImageUrlByToken(String token);
}
