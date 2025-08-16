package com.myce.system.dto.email;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExpoAdminEmailResponse {
    private String id;
    private String subject;
    private String content;
    private Integer recipientCount;
    private LocalDateTime createdAt;
}
