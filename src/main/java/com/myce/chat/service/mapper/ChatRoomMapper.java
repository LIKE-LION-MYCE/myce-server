package com.myce.chat.service.mapper;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.ChatRoomListResponse;

public class ChatRoomMapper {

    public static ChatRoomListResponse.ChatRoomInfo toDto(
            ChatRoom chatRoom,
            Long otherMemberId,
            String otherMemberName,
            String otherMemberRole,
            String expoTitle,
            int unreadCount
    ) {
        return ChatRoomListResponse.ChatRoomInfo.builder()
                .id(chatRoom.getId())
                .roomCode(chatRoom.getRoomCode())
                .expoId(chatRoom.getExpoId())
                .expoTitle(expoTitle)
                .otherMemberId(otherMemberId)
                .otherMemberName(otherMemberName)
                .otherMemberRole(otherMemberRole)
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unreadCount)
                .isActive(chatRoom.getIsActive())
                .currentAdminCode(chatRoom.getCurrentAdminCode())
                .adminDisplayName(chatRoom.getAdminDisplayName())
                .currentState(chatRoom.getCurrentState() != null ? chatRoom.getCurrentState().name() : null)
                .build();
    }
}
