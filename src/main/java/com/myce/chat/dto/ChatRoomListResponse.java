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
     * 기존 chatRoomList → chatRooms로 변경
     */
    private List<ChatRoomInfo> chatRooms;
    
    /**
     * 전체 채팅방 개수
     * 페이징 처리나 UI 표시용
     */
    private Integer totalCount;

    @Builder
    public ChatRoomListResponse(List<ChatRoomInfo> chatRooms, Integer totalCount) {
        this.chatRooms = chatRooms;
        this.totalCount = totalCount;
    }

    /**
     * 개별 채팅방 정보 내부 클래스
     * 
     * 포함 정보:
     * - 기본 채팅방 정보 (ID, 코드, 박람회명)
     * - 마지막 메시지 관련 정보
     * - 읽지 않은 메시지 개수
     * - 상대방 정보 (박람회 관리자 or 참가자)
     */
    @Getter
    @NoArgsConstructor
    public static class ChatRoomInfo {
        
        /**
         * 채팅방 고유 ID (MongoDB ObjectId)
         */
        private String id;
        
        /**
         * 채팅방 코드 (admin-{expoId}-{userId} 형식)
         * 프론트엔드에서 WebSocket 구독 시 사용
         */
        private String roomCode;
        
        /**
         * 박람회 ID
         */
        private Long expoId;
        
        /**
         * 박람회 제목
         * 채팅방 제목으로 표시됨
         */
        private String expoTitle;
        
        /**
         * 상대방 회원 ID
         * - 일반 사용자 입장: 박람회 관리자 ID
         * - 관리자 입장: 참가자 ID
         */
        private Long otherMemberId;
        
        /**
         * 상대방 이름
         * 채팅방 목록에서 상대방 표시명으로 사용
         */
        private String otherMemberName;
        
        /**
         * 상대방 역할 ("ADMIN" | "USER")
         * UI에서 아이콘이나 배지 표시용
         */
        private String otherMemberRole;
        
        /**
         * 마지막 메시지 내용
         * 채팅방 목록에서 미리보기로 표시
         */
        private String lastMessage;
        
        /**
         * 마지막 메시지 시간
         * "5분 전", "어제" 등으로 변환하여 표시
         */
        private LocalDateTime lastMessageAt;
        
        /**
         * 읽지 않은 메시지 개수
         * 0이면 UI에서 배지 숨김, 1 이상이면 빨간 배지 표시
         */
        private Integer unreadCount;
        
        /**
         * 채팅방 활성화 상태
         * false면 UI에서 비활성화 표시 또는 목록에서 제외
         */
        private Boolean isActive;

        @Builder
        public ChatRoomInfo(String id, String roomCode, Long expoId, String expoTitle,
                           Long otherMemberId, String otherMemberName, String otherMemberRole,
                           String lastMessage, LocalDateTime lastMessageAt,
                           Integer unreadCount, Boolean isActive) {
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
        }
    }
}