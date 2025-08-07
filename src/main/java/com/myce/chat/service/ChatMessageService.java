package com.myce.chat.service;

import com.myce.chat.document.ChatMessage;

/**
 * 채팅 메시지 생성 서비스
 */

public interface ChatMessageService {

    /**
     * 일반 채팅 메시지 생성
     */
    ChatMessage createMessage(String roomCode, String senderType, Long senderId, 
                            String senderName, String content);

    /**
     * 파일 포함 메시지 생성
     */
    ChatMessage createFileMessage(String roomCode, String senderType, Long senderId,
                                String senderName, String content, String messageType, 
                                String fileInfoJson);

    /**
     * 시스템 메시지 생성
     */
    ChatMessage createSystemMessage(String roomCode, String messageType, String content);

    /**
     * 입장 알림 메시지 생성
     */
    ChatMessage createEnterMessage(String roomCode, String memberName);

    /**
     * 퇴장 알림 메시지 생성
     */
    ChatMessage createLeaveMessage(String roomCode, String memberName);


}