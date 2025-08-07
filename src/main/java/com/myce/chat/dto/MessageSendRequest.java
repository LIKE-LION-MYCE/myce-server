package com.myce.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 전송 요청 DTO
 */
@Getter
@NoArgsConstructor
public class MessageSendRequest {
    
    private String roomId;   // "admin-{expoId}-{userId}" 형식
    private String content;  // 메시지 내용
    
    @Builder
    public MessageSendRequest(String roomId, String content) {
        this.roomId = roomId;
        this.content = content;
    }
}