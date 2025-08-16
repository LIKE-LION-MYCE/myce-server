package com.myce.qrcode.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.event.spi.ClearEventListener;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class QrVerifyResponse {
    private boolean valid;
    private String message;
    private String status;
    private String reserverName;
    private String expoTitle;
    private String ticketTitle;
    private LocalDateTime activatedAt;

    public static QrVerifyResponse success(String message, String reserverName,
                             String expoTitle, String ticketTitle, String status) {
        return builder()
                .valid(true)
                .message(message)
                .reserverName(reserverName)
                .expoTitle(expoTitle)
                .ticketTitle(ticketTitle)
                .status(status)
                .build();
    }

    public static QrVerifyResponse fail(String message, String status) {

        return builder()
                .valid(false)
                .message(message)
                .status(status)
                .build();
    }
}