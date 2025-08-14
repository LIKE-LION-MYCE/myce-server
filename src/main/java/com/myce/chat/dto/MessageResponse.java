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
    private String senderName; // 발신자 이름 (모든 메시지 타입에 사용)
    private String adminCode; // 관리자 메시지인 경우 관리자 코드
    private String adminDisplayName; // 관리자 메시지인 경우 관리자 표시명
    private String content;
    private LocalDateTime sentAt;
    private Integer unreadCount; // 카카오톡 스타일 읽지 않은 수 (0이면 읽음, 1이면 안읽음)
    
    @Builder
    public MessageResponse(String roomId, String messageId, Long senderId, String senderType,
                          String senderName, String adminCode, String adminDisplayName, String content, 
                          LocalDateTime sentAt, Integer unreadCount) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.adminCode = adminCode;
        this.adminDisplayName = adminDisplayName;
        this.content = content;
        this.sentAt = sentAt;
        this.unreadCount = unreadCount;
    }
}