package com.myce.chat.document;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Slf4j
@Getter
@NoArgsConstructor
@Document(collection = "chat_rooms")
@CompoundIndexes({
    @CompoundIndex(name = "member_active_idx", def = "{'memberId': 1, 'isActive': 1, 'lastMessageAt': -1}"),
    @CompoundIndex(name = "expo_active_idx", def = "{'expoId': 1, 'isActive': 1, 'lastMessageAt': -1}"),
    @CompoundIndex(name = "expo_member_idx", def = "{'expoId': 1, 'memberId': 1}", unique = true)
})
public class ChatRoom {

    @Id
    private String id;

    /**
     * 채팅방 코드 (비즈니스 키)
     * 형식: "admin-{expoId}-{memberId}"
    */
    @Indexed(unique = true)
    private String roomCode;

    /**
     * 참가자 회원 ID
     */
    @Indexed
    private Long memberId;

    /**
     * 참가자 이름 (캐시용)
     */
    private String memberName;

    /**
     * 박람회 ID
     */
    @Indexed
    private Long expoId;

    /**
     * 박람회 제목 (캐시용)
     */
    private String expoTitle;

    /**
     * 채팅방 활성화 상태
     */
    @Indexed
    private Boolean isActive;

    /**
     * 마지막 메시지 내용 (미리보기용)
     */
    private String lastMessage;

    /**
     * 마지막 메시지 ID
     */
    private String lastMessageId;

    /**
     * 마지막 메시지 전송 시간
     */
    @Indexed
    private LocalDateTime lastMessageAt;

    /**
     * 각 사용자별 마지막 읽은 메시지 정보 (JSON 형태)
     */
    private String readStatusJson;

    /**
     * 채팅방 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 채팅방 정보 최종 수정 시간
     */
    @LastModifiedDate  
    private LocalDateTime updatedAt;

    /**
     * 현재 담당 관리자 식별자
     * - AdminCode 로그인: AdminCode.code (예: "CODE123A") 
     * - Super Admin 로그인: "SUPER_ADMIN"
     * - 담당자 없음: null
     */
    @Setter
    private String currentAdminCode;

    /**
     * 마지막 관리자 활동 시간
     * 담당자 타임아웃 스케줄러에서 사용 (5분 비활성시 담당 해제)
     */
    @Setter
    private LocalDateTime lastAdminActivity;

    /**
     * 사용자에게 표시되는 관리자 이름
     * 예: "2024 IT박람회 관리자"
     */
    @Setter
    private String adminDisplayName;

    /**
     * 관리자 연결 대기 상태 (AI 핸드오프용)
     */
    @Setter
    private Boolean waitingForAdmin;

    /**
     * 핸드오프 요청 시간
     */
    @Setter  
    private LocalDateTime handoffRequestedAt;

    /**
     * 현재 채팅방 상태 (modular state storage)
     * DB에 실제 enum 값 저장하여 상태 관리 효율화
     */
    @Setter
    @Indexed
    private ChatRoomState currentState;

    /**
     * 채팅방 생성 시 기본값 설정
     */
    @Builder
    public ChatRoom(String roomCode, Long memberId, String memberName, 
                   Long expoId, String expoTitle) {
        this.roomCode = roomCode;
        this.memberId = memberId;
        this.memberName = memberName;
        this.expoId = expoId;
        this.expoTitle = expoTitle;
        this.isActive = true;  // 기본값: 활성화
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.readStatusJson = "{}";  // 빈 JSON 객체로 초기화
        this.waitingForAdmin = false;  // 기본값: 대기 상태 아님
        
        // roomCode 기반 초기 상태 분기 처리
        if (roomCode != null && roomCode.startsWith("platform-")) {
            // 플랫폼 채팅방: 기존 상태 유지 (신규 채팅만 AI로 시작)
            if (this.currentState == null) {
                this.currentState = ChatRoomState.AI_ACTIVE;  // 신규 채팅: AI로 시작
            }
            // 기존에 ADMIN_ACTIVE였다면 그대로 유지하여 상담 연속성 보장
        } else {
            this.currentState = null;  // 박람회: legacy fallback 사용 (담당자 배정 로직)
        }
    }

    /**
     * 새 메시지 발송 시 채팅방 정보 업데이트
     */
    public void updateLastMessageInfo(String messageId, String messageContent) {
        this.lastMessageId = messageId;
        this.lastMessage = truncateMessage(messageContent);
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 재활성화
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 메시지 내용 자르기 (미리보기용)
     */
    private String truncateMessage(String content) {
        if (content == null) return null;
        if (content.length() <= 200) return content;
        return content.substring(0, 197) + "...";
    }

    /**
     * 담당자 배정 (atomic assignment with collision protection)
     * @param adminCode AdminCode.code 또는 "SUPER_ADMIN"
     * @return true if successfully assigned, false if already has admin (collision)
     */
    public boolean assignAdmin(String adminCode) {
        // Collision protection: prevent assignment if admin already exists
        if (this.currentAdminCode != null && !this.currentAdminCode.equals(adminCode)) {
            log.warn("Admin collision prevented: Room {} already has admin {}, cannot assign {}", 
                     roomCode, this.currentAdminCode, adminCode);
            return false;
        }
        
        this.currentAdminCode = adminCode;
        this.lastAdminActivity = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Update modular state
        this.currentState = ChatRoomState.ADMIN_ACTIVE;
        this.waitingForAdmin = false;  // Sync legacy field
        
        return true;
    }
    
    /**
     * 관리자 권한 확인 (permission check)
     * @param adminCode 확인할 관리자 코드
     * @return true if this admin has permission for this room
     */
    public boolean hasAdminPermission(String adminCode) {
        return this.currentAdminCode != null && this.currentAdminCode.equals(adminCode);
    }

    /**
     * 담당자 해제
     */
    public void releaseAdmin() {
        this.currentAdminCode = null;
        this.lastAdminActivity = null;
        this.updatedAt = LocalDateTime.now();
        
        // Update modular state
        this.currentState = ChatRoomState.AI_ACTIVE;
        this.waitingForAdmin = false;  // Sync legacy field
    }

    /**
     * 관리자 활동 시간 업데이트
     */
    public void updateAdminActivity() {
        if (this.currentAdminCode != null) {
            this.lastAdminActivity = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 담당자가 있는지 확인
     */
    public boolean hasAssignedAdmin() {
        return this.currentAdminCode != null;
    }
    
    /**
     * 읽음 상태 업데이트
     */
    public void updateReadStatus(String readStatusJson) {
        this.readStatusJson = readStatusJson;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 관리자 연결 대기 시작
     */
    public void startWaitingForAdmin() {
        this.waitingForAdmin = true;
        this.handoffRequestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Update modular state
        this.currentState = ChatRoomState.WAITING_FOR_ADMIN;
    }
    
    /**
     * 관리자 연결 대기 종료
     */
    public void stopWaitingForAdmin() {
        this.waitingForAdmin = false;
        this.handoffRequestedAt = null;
        this.updatedAt = LocalDateTime.now();
        
        // Update modular state (return to AI if no admin assigned)
        this.currentState = hasAssignedAdmin() ? ChatRoomState.ADMIN_ACTIVE : ChatRoomState.AI_ACTIVE;
    }
    
    /**
     * 현재 관리자 연결 대기 중인지 확인
     */
    public boolean isWaitingForAdmin() {
        return Boolean.TRUE.equals(this.waitingForAdmin);
    }
    
    /**
     * 채팅방 상태 enum (3-state system)
     */
    public enum ChatRoomState {
        AI_ACTIVE("AI 상담 중", "Request Human"),
        WAITING_FOR_ADMIN("상담원 대기 중", "Cancel Request"), 
        ADMIN_ACTIVE("상담원 상담 중", "Request AI");
        
        private final String description;
        private final String buttonText;
        
        ChatRoomState(String description, String buttonText) {
            this.description = description;
            this.buttonText = buttonText;
        }
        
        public String getDescription() { return description; }
        public String getButtonText() { return buttonText; }
    }
    
    /**
     * 현재 채팅방 상태 확인 (modular state storage with legacy fallback)
     * Note: 10분 비활성 관리자는 스케줄러에서 자동으로 해제됨
     */
    public ChatRoomState getCurrentState() {
        // Use stored state if available (new modular approach)
        if (currentState != null) {
            return currentState;
        }
        
        // Fallback to legacy boolean logic for existing rooms
        if (isWaitingForAdmin()) {
            return ChatRoomState.WAITING_FOR_ADMIN;
        } else if (hasAssignedAdmin()) {
            return ChatRoomState.ADMIN_ACTIVE;
        } else {
            // roomCode 기반 분기: 박람회 vs 플랫폼
            if (roomCode != null && roomCode.startsWith("admin-")) {
                // 박람회 채팅방: 담당자 미배정시 대기 상태
                return ChatRoomState.WAITING_FOR_ADMIN;
            } else {
                // 플랫폼 채팅방: AI 활성 상태 (기존 로직)
                return ChatRoomState.AI_ACTIVE;
            }
        }
    }
    
    /**
     * 관리자가 10분 이상 비활성 상태인지 확인 (스케줄러용)
     */
    public boolean isAdminInactiveForTenMinutes() {
        return hasAssignedAdmin() && 
               lastAdminActivity != null && 
               lastAdminActivity.isBefore(LocalDateTime.now().minusMinutes(10));
    }
    
    /**
     * 상태 직접 변경 (modular state management)
     * 상태 변경 시 관련 필드도 자동으로 동기화
     */
    public void transitionToState(ChatRoomState newState) {
        this.currentState = newState;
        this.updatedAt = LocalDateTime.now();
        
        // Sync legacy fields based on new state
        switch (newState) {
            case AI_ACTIVE:
                this.waitingForAdmin = false;
                break;
            case WAITING_FOR_ADMIN:
                this.waitingForAdmin = true;
                if (this.handoffRequestedAt == null) {
                    this.handoffRequestedAt = LocalDateTime.now();
                }
                break;
            case ADMIN_ACTIVE:
                this.waitingForAdmin = false;
                this.handoffRequestedAt = null;
                if (this.lastAdminActivity == null) {
                    this.lastAdminActivity = LocalDateTime.now();
                }
                break;
        }
    }
}
