package com.myce.qrcode.service;

import com.myce.qrcode.entity.QrCode;
import com.myce.reservation.entity.Reserver;

/**
 * QR 코드 생성 서비스
 */
public interface QrCodeGenerateService {

    QrCode createQrCode(Reserver reserver);
}
