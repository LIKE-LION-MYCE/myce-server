package com.myce.member.dto.expo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpoAdminCodeResponse {
    
    private Long adminCodeId;
    private String code;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}