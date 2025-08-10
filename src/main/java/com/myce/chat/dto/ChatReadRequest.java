package com.myce.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 읽음 처리 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ChatReadRequest {

    /**
     * 마지막으로 읽은 메시지 ID
     */
    private String lastReadMessageId;
    
    @Builder
    public ChatReadRequest(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}