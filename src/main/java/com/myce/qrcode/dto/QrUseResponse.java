package com.myce.qrcode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QrUseResponse {
    private boolean isSuccess;
    private String message;
}