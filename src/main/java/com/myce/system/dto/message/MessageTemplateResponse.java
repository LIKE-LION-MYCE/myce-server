package com.myce.system.dto.message;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageTemplateResponse {
    private Long id;
    private String name;
    private String channelType;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
