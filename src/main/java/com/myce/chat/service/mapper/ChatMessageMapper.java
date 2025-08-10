package com.myce.chat.service.mapper;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.MessageResponse;

public class ChatMessageMapper {

    public static MessageResponse toDto(ChatMessage chatMessage) {
        return MessageResponse.builder()
                .roomId(chatMessage.getRoomCode())
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .senderType(chatMessage.getSenderType())
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
                .content(savedMessage.getContent())
                .sentAt(savedMessage.getSentAt())
                .build();
    }
}