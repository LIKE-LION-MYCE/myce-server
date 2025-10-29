package com.myce.qrcode.service;

/**
 * QR 이미지 생성 서비스
 */
public interface QrImageGenerateService {

    byte[] generateQrImage(String token);
}
