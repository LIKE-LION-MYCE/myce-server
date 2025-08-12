package com.myce.qrcode.entity.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QrCodeStatus {
    APPROVED("QR 코드가 활성화 되지 않았습니다"),
    ACTIVE("유효한 QR 코드입니다"),
    USED("이미 사용된 QR 코드 입니다"),
    EXPIRED("만료된 QR 코드 입니다");
    
    private final String message;
}