package com.myce.chat.enums;

/**
 * 채팅 메시지 발송자 타입 Enum
 */
public enum MessageSenderType {
    
    /**
     * 시스템
     * - 시스템에서 자동 생성하는 메시지용
     */
    SYSTEM("시스템");
    
    private final String description;
    
    MessageSenderType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}