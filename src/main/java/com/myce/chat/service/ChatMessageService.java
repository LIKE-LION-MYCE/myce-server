package com.myce.chat.service;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.type.MessageSenderType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 생성 서비스
 * 
 * 메시지 생성 로직을 중앙화하여 일관성 보장
 */
@Service
public class ChatMessageService {

    /**
     * 기본 메시지 타입
     */
    public static final String DEFAULT_MESSAGE_TYPE = "TEXT";
    public static final String EMPTY_JSON = "{}";
    public static final Long SYSTEM_SENDER_ID = 0L;

    /**
     * 일반 채팅 메시지 생성
     */
    public ChatMessage createMessage(String roomCode, String senderType, Long senderId, 
                                   String senderName, String content) {
        return createMessage(roomCode, senderType, senderId, senderName, content, 
                           false, DEFAULT_MESSAGE_TYPE, null);
    }

    /**
     * 파일 포함 메시지 생성
     */
    public ChatMessage createFileMessage(String roomCode, String senderType, Long senderId,
                                       String senderName, String content, String messageType, 
                                       String fileInfoJson) {
        return createMessage(roomCode, senderType, senderId, senderName, content,
                           false, messageType, fileInfoJson);
    }

    /**
     * 시스템 메시지 생성
     */
    public ChatMessage createSystemMessage(String roomCode, String messageType, String content) {
        return createMessage(roomCode, MessageSenderType.SYSTEM.name(), SYSTEM_SENDER_ID,
                           MessageSenderType.SYSTEM.getDescription(), content, true, messageType, null);
    }

    /**
     * 입장 알림 메시지 생성
     */
    public ChatMessage createEnterMessage(String roomCode, String memberName) {
        String content = String.format(ChatRoomService.ENTER_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, "SYSTEM_ENTER", content);
    }

    /**
     * 퇴장 알림 메시지 생성
     */
    public ChatMessage createLeaveMessage(String roomCode, String memberName) {
        String content = String.format(ChatRoomService.LEAVE_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, "SYSTEM_LEAVE", content);
    }

    /**
     * 메시지 생성 핵심 로직
     */
    private ChatMessage createMessage(String roomCode, String senderType, Long senderId,
                                    String senderName, String content, Boolean isSystemMessage,
                                    String messageType, String fileInfoJson) {
        return ChatMessage.builder()
                .roomCode(roomCode)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .isSystemMessage(isSystemMessage != null ? isSystemMessage : false)
                .messageType(messageType != null ? messageType : DEFAULT_MESSAGE_TYPE)
                .fileInfoJson(fileInfoJson)
                .build();
    }
}