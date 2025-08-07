package com.myce.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 입장 요청 DTO
 */
@Getter
@NoArgsConstructor
public class JoinRoomRequest {
    
    private String roomId;  // "admin-{expoId}-{userId}" 형식
    
    @Builder
    public JoinRoomRequest(String roomId) {
        this.roomId = roomId;
    }
}