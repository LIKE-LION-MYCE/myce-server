package com.myce.chat.service.impl;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.type.MessageSenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 채팅 메시지 생성 서비스 구현체
 * 
 * 메시지 생성 로직을 중앙화하여 일관성 보장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    /**
     * 기본 메시지 타입
     */
    public static final String DEFAULT_MESSAGE_TYPE = "TEXT";
    public static final String EMPTY_JSON = "{}";
    public static final Long SYSTEM_SENDER_ID = 0L;
    
    /**
     * 시스템 메시지 타입
     */
    private static final String SYSTEM_ENTER_TYPE = "SYSTEM_ENTER";
    private static final String SYSTEM_LEAVE_TYPE = "SYSTEM_LEAVE";

    @Override
    public ChatMessage createMessage(String roomCode, String senderType, Long senderId, 
                                   String senderName, String content) {
        log.debug("일반 메시지 생성 - roomCode: {}, senderId: {}", roomCode, senderId);
        return createMessage(roomCode, senderType, senderId, senderName, content, 
                           false, DEFAULT_MESSAGE_TYPE, null);
    }

    @Override
    public ChatMessage createFileMessage(String roomCode, String senderType, Long senderId,
                                       String senderName, String content, String messageType, 
                                       String fileInfoJson) {
        log.debug("파일 메시지 생성 - roomCode: {}, senderId: {}, messageType: {}", 
                  roomCode, senderId, messageType);
        return createMessage(roomCode, senderType, senderId, senderName, content,
                           false, messageType, fileInfoJson);
    }

    @Override
    public ChatMessage createSystemMessage(String roomCode, String messageType, String content) {
        log.debug("시스템 메시지 생성 - roomCode: {}, messageType: {}", roomCode, messageType);
        return createMessage(roomCode, MessageSenderType.SYSTEM.name(), SYSTEM_SENDER_ID,
                           MessageSenderType.SYSTEM.getDescription(), content, true, messageType, null);
    }

    @Override
    public ChatMessage createEnterMessage(String roomCode, String memberName) {
        log.debug("입장 알림 메시지 생성 - roomCode: {}, memberName: {}", roomCode, memberName);
        String content = String.format(ChatRoomService.ENTER_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, SYSTEM_ENTER_TYPE, content);
    }

    @Override
    public ChatMessage createLeaveMessage(String roomCode, String memberName) {
        log.debug("퇴장 알림 메시지 생성 - roomCode: {}, memberName: {}", roomCode, memberName);
        String content = String.format(ChatRoomService.LEAVE_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, SYSTEM_LEAVE_TYPE, content);
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