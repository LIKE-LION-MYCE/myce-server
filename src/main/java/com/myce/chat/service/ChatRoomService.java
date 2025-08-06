package com.myce.chat.service;

import com.myce.chat.dto.ChatRoomListResponse;

/**
 * 채팅방 비즈니스 로직 서비스
 */
public interface ChatRoomService {

    /**
     * 사용자별 채팅방 목록 조회 (USER: 본인 참여, ADMIN: 관리 박람회 전체)
     */
    ChatRoomListResponse getChatRooms(Long memberId, String memberRole);

    /**
     * 박람회별 채팅방 목록 조회 (관리자 전용, 권한 검증)
     */
    ChatRoomListResponse getChatRoomsByExpo(Long expoId, Long adminId);
}