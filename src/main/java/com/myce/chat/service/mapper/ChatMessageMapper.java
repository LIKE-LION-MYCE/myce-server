package com.myce.chat.service.mapper;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class ChatMessageMapper {

    public static MessageResponse toDto(ChatMessage chatMessage) {
        return MessageResponse.builder()
                .roomId(chatMessage.getRoomCode())
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .senderType(chatMessage.getSenderType())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .unreadCount(null) // unreadCount 없이 호출된 경우 null
                .build();
    }
    
    /**
     * unreadCount를 포함한 DTO 변환
     */
    public static MessageResponse toDto(ChatMessage chatMessage, Integer unreadCount) {
        return MessageResponse.builder()
                .roomId(chatMessage.getRoomCode())
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .senderType(chatMessage.getSenderType())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 관리자 정보와 unreadCount를 포함한 DTO 변환
     */
    public static MessageResponse toDto(ChatMessage chatMessage, Integer unreadCount, String adminCode, String adminDisplayName) {
        return MessageResponse.builder()
                .roomId(chatMessage.getRoomCode())
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .senderType(chatMessage.getSenderType())
                .senderName(chatMessage.getSenderName())
                .adminCode(adminCode)
                .adminDisplayName(adminDisplayName)
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .unreadCount(unreadCount)
                .build();
    }

    public static MessageResponse toSendResponse(ChatMessage savedMessage, String roomId) {
        return MessageResponse.builder()
                .roomId(roomId)
                .messageId(savedMessage.getId())
                .senderId(savedMessage.getSenderId())
                .senderType(savedMessage.getSenderType())
                .senderName(savedMessage.getSenderName())
                .content(savedMessage.getContent())
                .sentAt(savedMessage.getSentAt())
                .build();
    }
}