package com.myce.qrcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QrVerifyResponse {
    private boolean valid;
    private String message;
    private String status;
    private String reserverName;
    private String expoTitle;
    private String ticketTitle;
    private LocalDateTime activatedAt;

    public QrVerifyResponse (String message, String reserverName,
                             String expoTitle, String ticketTitle, String status) {
        this.valid = true;
        this.message = message;
        this.reserverName = reserverName;
        this.expoTitle = expoTitle;
        this.ticketTitle = ticketTitle;
        this.status = status;
    }
    public QrVerifyResponse (String message, String status) {
        this.valid = false;
        this.message = message;
        this.status = status;
    }
}