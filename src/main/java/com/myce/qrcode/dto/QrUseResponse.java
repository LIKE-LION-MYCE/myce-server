package com.myce.qrcode.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class QrUseResponse {
    private boolean success;
    private String message;
    private String ticketName;
    private LocalDateTime usedAt;


    public static QrUseResponse success(String message, String ticketName) {
        return builder()
                .success(true)
                .message(message)
                .ticketName(ticketName)
                .usedAt(LocalDateTime.now())
                .build();

    }

    public static QrUseResponse fail(String message) {
        return builder()
                .success(false)
                .message(message)
                .usedAt(LocalDateTime.now())
                .build();

    }
}