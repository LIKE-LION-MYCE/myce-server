package com.myce.chat.service;

import com.myce.chat.dto.MessageResponse;

/**
 * 채팅 WebSocket 서비스
 * 
 * 실시간 메시지 송수신 처리
 */
public interface ChatWebSocketService {

    /**
     * JWT 토큰으로 사용자 인증
     */
    Long authenticateUser(String token);

    /**
     * 채팅방 입장 권한 검증 및 처리
     */
    void joinRoom(Long userId, String roomId);

    /**
     * 메시지 전송 및 저장
     */
    MessageResponse sendMessage(Long userId, String roomId, String content);
}