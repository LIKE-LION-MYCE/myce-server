package com.myce.chat.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket 메시지 타입 Enum
 * 
 * CRM-189 WebSocket 실시간 메시지 송수신용
 */
@Getter
@RequiredArgsConstructor
public enum WebSocketMessageType {
    
    /**
     * 인증 요청
     */
    AUTH("인증"),
    
    /**
     * 인증 응답
     */
    AUTH_ACK("인증응답"),
    
    /**
     * 채팅방 입장
     */
    JOIN_ROOM("방입장"),
    
    /**
     * 메시지 전송 요청
     */
    MESSAGE_SEND("메시지전송"),
    
    /**
     * 메시지 수신
     */
    MESSAGE("메시지"),
    
    /**
     * 에러
     */
    ERROR("에러"),
    
    /**
     * 담당자 해제 알림
     */
    ADMIN_RELEASED("담당자해제");
    
    private final String description;
    
    public static WebSocketMessageType fromString(String type) {
        for (WebSocketMessageType t : WebSocketMessageType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        throw new CustomException(CustomErrorCode.CHAT_SENDER_TYPE_INVALID);
    }
    
    public static WebSocketMessageType fromDescription(String description) {
        for (WebSocketMessageType t : WebSocketMessageType.values()) {
            if (t.getDescription().equals(description)) return t;
        }
        throw new CustomException(CustomErrorCode.CHAT_SENDER_TYPE_INVALID);
    }
}