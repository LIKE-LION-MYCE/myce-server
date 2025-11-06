package com.myce.chat.service;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import java.util.Map;

/**
 * WebSocket 메시지 브로드캐스트 서비스
 *
 * 채팅 메시지, 상태 업데이트, 에러 등을 WebSocket으로 브로드캐스트하는 기능 담당
 */
public interface ChatWebSocketBroadcaster {

    /**
     * 사용자 메시지 브로드캐스트
     */
    void broadcastUserMessage(String roomId, MessageResponse messageResponse, ChatRoom chatRoom);

    /**
     * 관리자 메시지 브로드캐스트
     */
    void broadcastAdminMessage(String roomCode, MessageResponse messageResponse, ChatRoom chatRoom, String adminCode);

    /**
     * 시스템 메시지 브로드캐스트 (핸드오프 수락 등)
     */
    void broadcastSystemMessage(String roomId, String messageType, Map<String, Object> payload);

    /**
     * 버튼 상태 업데이트 브로드캐스트
     */
    void broadcastButtonStateUpdate(String roomId, String newState);

    /**
     * 읽음 상태 업데이트 브로드캐스트
     */
    void broadcastReadStatusUpdate(String roomId, String messageId, Long readBy, String readerType);

    /**
     * 미읽음 카운트 업데이트 브로드캐스트 (박람회 관리자용)
     */
    void broadcastUnreadCountUpdate(Long expoId, String roomCode, Long unreadCount);

    /**
     * 담당자 배정 브로드캐스트
     */
    void broadcastAdminAssignment(String roomCode, ChatRoom chatRoom, Long expoId);

    /**
     * 에러 메시지 브로드캐스트
     */
    void broadcastError(String sessionId, Long userId, String errorMessage);

    /**
     * 커스텀 에러 메시지 브로드캐스트 (CustomException)
     */
    void broadcastCustomError(String sessionId, String errorCode, String errorMessage);
}
