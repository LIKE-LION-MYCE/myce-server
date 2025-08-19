package com.myce.chat.service;

/**
 * 채팅 읽지 않은 메시지 계산 통합 서비스
 * 
 * 목적: 모든 unread count 계산을 하나의 서비스로 통합하여 일관성 보장
 * 방식: 카카오톡 방식 - "내가 보낸 메시지를 상대방이 읽었는가?"
 */
public interface ChatUnreadService {
    
    /**
     * 특정 채팅방에서 특정 사용자 관점의 읽지 않은 메시지 수 계산
     * 
     * @param roomCode 채팅방 코드 (예: "admin-9-15", "platform-123")
     * @param viewerId 조회하는 사용자 ID
     * @param viewerRole 조회하는 사용자 역할 ("USER", "PLATFORM_ADMIN", "EXPO_ADMIN")
     * @return 읽지 않은 메시지 수 (내가 읽어야 할 메시지 개수)
     */
    Long getUnreadCountForViewer(String roomCode, Long viewerId, String viewerRole);
    
    /**
     * 특정 메시지의 읽지 않은 수 계산 (개별 메시지용)
     * 카카오톡 방식: 메시지 발송자 기준으로 상대방이 읽었는지 확인
     * 
     * @param messageId 메시지 ID
     * @param messageSenderId 메시지 발송자 ID  
     * @param messageSenderType 메시지 발송자 타입 ("USER", "ADMIN", "AI", "SYSTEM")
     * @param roomCode 채팅방 코드
     * @return 0 (읽음) 또는 1 (안읽음)
     */
    Integer getMessageUnreadCount(String messageId, Long messageSenderId, String messageSenderType, String roomCode);
    
    /**
     * 사용자의 전체 읽지 않은 메시지 배지 카운트 계산
     * FloatingChatButton에서 사용
     * 
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @return 전체 읽지 않은 메시지 수
     */
    Long getTotalUnreadBadgeCount(Long userId, String userRole);
    
    /**
     * readStatusJson에서 특정 사용자 타입의 마지막 읽은 메시지 ID 추출
     * 
     * @param readStatusJson JSON 문자열 (예: "{\"USER\":\"messageId\", \"ADMIN\":\"messageId\"}")
     * @param userType 사용자 타입 ("USER", "ADMIN", "AI")
     * @return 마지막 읽은 메시지 ID (없으면 null)
     */
    String extractLastReadMessageId(String readStatusJson, String userType);
}