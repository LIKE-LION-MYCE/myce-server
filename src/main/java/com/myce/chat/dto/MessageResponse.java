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
    private String senderType;
    private String content;
    private LocalDateTime sentAt;
    private Integer unreadCount; // 카카오톡 스타일 읽지 않은 수 (0이면 읽음, 1이면 안읽음)
    
    @Builder
    public MessageResponse(String roomId, String messageId, Long senderId, String senderType,
                          String content, LocalDateTime sentAt, Integer unreadCount) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.content = content;
        this.sentAt = sentAt;
        this.unreadCount = unreadCount;
    }
}