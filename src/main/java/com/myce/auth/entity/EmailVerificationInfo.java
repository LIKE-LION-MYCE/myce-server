package com.myce.auth.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationInfo {
    private String email;
    private String code;
    private LocalDateTime sendTime;
}
