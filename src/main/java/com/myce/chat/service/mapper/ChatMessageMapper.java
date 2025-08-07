package com.myce.chat.service.mapper;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.MessageResponse;

public class ChatMessageMapper {

    public static MessageResponse toDto(ChatMessage chatMessage) {
        return MessageResponse.builder()
                .roomId(chatMessage.getRoomCode())
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getSenderId())
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .build();
    }

    public static MessageResponse toSendResponse(ChatMessage savedMessage, String roomId) {
        return MessageResponse.builder()
                .roomId(roomId)
                .messageId(savedMessage.getId())
                .senderId(savedMessage.getSenderId())
                .content(savedMessage.getContent())
                .sentAt(savedMessage.getSentAt())
                .build();
    }
}