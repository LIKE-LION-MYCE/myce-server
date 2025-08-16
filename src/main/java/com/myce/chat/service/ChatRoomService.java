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
    
    /**
     * 사용자 채팅방 읽음 처리 (USER 타입 사용자 전용)
     */
    void markAsRead(String roomCode, String lastReadMessageId, Long memberId, String memberRole);
    
    /**
     * AI 상담을 관리자에게 인계 (요약 포함)
     */
    void handoffAIToAdmin(String roomCode, String adminCode);
    
    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회 (역할 기반 접근 제어)
     * @param roomCode 채팅방 코드
     * @param memberId 조회 요청자 ID
     * @param memberRole 조회 요청자 역할
     * @return 읽지 않은 메시지 수
     */
    Long getUnreadCount(String roomCode, Long memberId, String memberRole);
    
    /**
     * 채팅방 접근 권한 검증 (메시지 조회, 읽음 처리 등에 사용)
     * @param roomCode 채팅방 코드
     * @param memberId 조회 요청자 ID
     * @param memberRole 조회 요청자 역할
     * @throws CustomException 권한이 없을 경우
     */
    void validateChatRoomAccess(String roomCode, Long memberId, String memberRole);
}