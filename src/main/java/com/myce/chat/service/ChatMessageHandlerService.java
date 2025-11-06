package com.myce.chat.service;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import java.util.Map;

/**
 * 메시지 핸들링 서비스
 *
 * WebSocket 메시지 수신 후 비즈니스 로직 처리
 * - AI 응답 처리
 * - 자동 읽음 처리
 * - 상태별 관리자 처리
 */
public interface ChatMessageHandlerService {

    /**
     * 사용자 메시지 플로우 처리
     * - AI 응답 (필요시)
     * - 자동 읽음 처리 (필요시)
     * - 미읽음 카운트 업데이트
     */
    void handleUserMessageFlow(Long userId, String roomId, String content, MessageResponse messageResponse);

    /**
     * AI 응답 처리 (플랫폼 채팅만 해당)
     */
    MessageResponse handleAIResponse(String roomId, String content);

    /**
     * 자동 읽음 처리 (플랫폼 상담 중일 때)
     */
    void handleAutoReadLogic(Long userId, String roomId, String messageId, ChatRoom currentRoom);

    /**
     * 미읽음 카운트 업데이트 처리
     */
    void handleUnreadCountUpdate(String roomId);

    /**
     * 관리자 메시지 상태별 처리
     * - WAITING_FOR_ADMIN: 핸드오프 실행
     * - AI_ACTIVE: 차단 및 에러 반환
     * - ADMIN_ACTIVE: 활동 업데이트
     */
    void handleAdminStateTransition(ChatRoom chatRoom, String adminCode, Long userId, Map<String, Object> errorHandler);

    /**
     * 권한 검증: 관리자가 이미 배정된 경우
     */
    boolean validateAdminPermission(ChatRoom chatRoom, String adminCode, Long userId, Map<String, Object> errorHandler);
}
