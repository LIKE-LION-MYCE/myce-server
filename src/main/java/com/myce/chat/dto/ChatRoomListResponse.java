package com.myce.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 목록 조회 응답 DTO
 * 
 * 구조:
 * - chatRooms: 채팅방 정보 목록 (복수형 네이밍 적용)
 * - totalCount: 전체 채팅방 개수
 * 
 * @author MYCE Team
 * @since 2025-08-06
 */
@Getter
@NoArgsConstructor
public class ChatRoomListResponse {

    /**
     * 채팅방 목록 (복수형 네이밍)
     */
    private List<ChatRoomInfo> chatRooms;
    
    /**
     * 전체 채팅방 개수
     */
    private Integer totalCount;

    @Builder
    public ChatRoomListResponse(List<ChatRoomInfo> chatRooms, Integer totalCount) {
        this.chatRooms = chatRooms;
        this.totalCount = totalCount;
    }

    /**
     * 개별 채팅방 정보 내부 클래스
     */
    @Getter
    @NoArgsConstructor
    public static class ChatRoomInfo {
        
        /**
         * 채팅방 고유 ID
         */
        private String id;
        
        /**
         * 채팅방 코드 (admin-{expoId}-{userId} 형식)
         */
        private String roomCode;
        
        /**
         * 박람회 ID
         */
        private Long expoId;
        
        /**
         * 박람회 제목
         */
        private String expoTitle;
        
        /**
         * 상대방 회원 ID
         */
        private Long otherMemberId;
        
        /**
         * 상대방 이름
         */
        private String otherMemberName;
        
        /**
         * 상대방 역할 (ChatMemberRole enum 값)
         */
        private String otherMemberRole;
        
        /**
         * 마지막 메시지 내용
         */
        private String lastMessage;
        
        /**
         * 마지막 메시지 시간
         */
        private LocalDateTime lastMessageAt;
        
        /**
         * 읽지 않은 메시지 개수
         */
        private Integer unreadCount;
        
        /**
         * 채팅방 활성화 상태
         */
        private Boolean isActive;
        
        /**
         * 현재 담당 관리자 코드
         */
        private String currentAdminCode;
        
        /**
         * 담당 관리자 표시명 (관리자용)
         */
        private String adminDisplayName;
        
        /**
         * 현재 채팅방 상태 (AI_ACTIVE, WAITING_FOR_ADMIN, ADMIN_ACTIVE)
         */
        private String currentState;

        @Builder
        public ChatRoomInfo(String id, String roomCode, Long expoId, String expoTitle,
                           Long otherMemberId, String otherMemberName, String otherMemberRole,
                           String lastMessage, LocalDateTime lastMessageAt,
                           Integer unreadCount, Boolean isActive,
                           String currentAdminCode, String adminDisplayName, String currentState) {
            this.id = id;
            this.roomCode = roomCode;
            this.expoId = expoId;
            this.expoTitle = expoTitle;
            this.otherMemberId = otherMemberId;
            this.otherMemberName = otherMemberName;
            this.otherMemberRole = otherMemberRole;
            this.lastMessage = lastMessage;
            this.lastMessageAt = lastMessageAt;
            this.unreadCount = unreadCount != null ? unreadCount : 0;
            this.isActive = isActive != null ? isActive : true;
            this.currentAdminCode = currentAdminCode;
            this.adminDisplayName = adminDisplayName;
            this.currentState = currentState;
        }
    }
}