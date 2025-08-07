package com.myce.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 응답 DTO
 */
@Getter
@NoArgsConstructor
public class MessageResponse {
    
    private String roomId;
    private String messageId;
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;
    
    @Builder
    public MessageResponse(String roomId, String messageId, Long senderId, 
                          String content, LocalDateTime sentAt) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
    }
}