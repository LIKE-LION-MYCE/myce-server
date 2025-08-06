package com.myce.chat.service;

import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.member.entity.type.Role;

/**
 * 채팅방 비즈니스 로직 서비스
 * 
 * 역할별 권한 검증 및 채팅방 조회 로직을 제공합니다.
 */
public interface ChatRoomService {

    /**
     * 관리자 역할 문자열
     */
    String ADMIN_ROLE = Role.EXPO_ADMIN.name();
    
    /**
     * 일반 사용자 역할 문자열  
     */
    String USER_ROLE = Role.USER.name();
    
    /**
     * 시스템 메시지 형식
     */
    String ENTER_MESSAGE_FORMAT = "%s님이 채팅방에 입장하셨습니다.";
    String LEAVE_MESSAGE_FORMAT = "%s님이 채팅방을 나가셨습니다.";

    /**
     * 사용자별 채팅방 목록 조회 (USER: 본인 참여, ADMIN: 관리 박람회 전체)
     */
    ChatRoomListResponse getChatRooms(Long memberId, String memberRole);

    /**
     * 박람회별 채팅방 목록 조회 (관리자 전용, 권한 검증)
     */
    ChatRoomListResponse getChatRoomsByExpo(Long expoId, Long adminId);
    
    /**
     * 역할 기반 권한 검증
     */
    default boolean isAdmin(String memberRole) {
        return ADMIN_ROLE.equals(memberRole);
    }
    
    /**
     * 시스템 메시지 생성
     */
    default String getEnterMessage(String memberName) {
        return String.format(ENTER_MESSAGE_FORMAT, memberName);
    }
    
    default String getLeaveMessage(String memberName) {
        return String.format(LEAVE_MESSAGE_FORMAT, memberName);
    }
}