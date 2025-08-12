package com.myce.qrcode.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QrVerifyResponse {
    private boolean valid;
    private String message;
    private String status;
    private String reserverName;
    private String expoTitle;
    private String ticketTitle;
    private LocalDateTime activatedAt;
    
    public static QrVerifyResponse valid(String reserverName, String expoTitle, String ticketTitle) {
        return QrVerifyResponse.builder()
                .valid(true)
                .message("유효한 QR 코드입니다.")
                .status("ACTIVE")
                .reserverName(reserverName)
                .expoTitle(expoTitle)
                .ticketTitle(ticketTitle)
                .build();
    }
    
    public static QrVerifyResponse invalid(String message, String status) {
        return QrVerifyResponse.builder()
                .valid(false)
                .message(message)
                .status(status)
                .build();
    }
    
    public static QrVerifyResponse notActive(String message, LocalDateTime activatedAt) {
        return QrVerifyResponse.builder()
                .valid(false)
                .message(message)
                .status("NOT_ACTIVE")
                .activatedAt(activatedAt)
                .build();
    }
}