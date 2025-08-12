package com.myce.qrcode.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QrUseResponse {
    private boolean success;
    private String message;
    private String reserverName;
    private String expoTitle;
    private String ticketTitle;
    
    public static QrUseResponse success(String reserverName, String expoTitle, String ticketTitle) {
        return QrUseResponse.builder()
                .success(true)
                .message("QR 코드가 성공적으로 사용 처리되었습니다.")
                .reserverName(reserverName)
                .expoTitle(expoTitle)
                .ticketTitle(ticketTitle)
                .build();
    }
}