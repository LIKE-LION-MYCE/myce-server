package com.myce.chat.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지 발송자 타입 Enum
 */
@Getter
@RequiredArgsConstructor
public enum MessageSenderType {
    
    /**
     * 시스템
     * - 시스템에서 자동 생성하는 메시지용
     */
    SYSTEM("시스템");
    
    private final String description;
    
    public static MessageSenderType fromString(String type) {
        for (MessageSenderType t : MessageSenderType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        throw new CustomException(CustomErrorCode.CHAT_SENDER_TYPE_INVALID);
    }
    
    public static MessageSenderType fromDescription(String description) {
        for (MessageSenderType t : MessageSenderType.values()) {
            if (t.getDescription().equals(description)) return t;
        }
        throw new CustomException(CustomErrorCode.CHAT_SENDER_TYPE_INVALID);
    }
}