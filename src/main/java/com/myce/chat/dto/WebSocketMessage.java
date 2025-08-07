package com.myce.chat.dto;

import com.myce.chat.type.WebSocketMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * WebSocket 메시지 기본 구조
 * 
 * CRM-189 WebSocket 실시간 메시지 송수신용
 */
@Getter
@NoArgsConstructor
public class WebSocketMessage {
    
    private WebSocketMessageType type;
    private Object payload;
    
    @Builder
    public WebSocketMessage(WebSocketMessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}