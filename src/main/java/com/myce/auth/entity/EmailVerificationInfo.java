package com.myce.auth.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash("email_verification_info")
@AllArgsConstructor
public class EmailVerificationInfo {
    private String email;
    private String code;
    private LocalDateTime sendTime;
}
