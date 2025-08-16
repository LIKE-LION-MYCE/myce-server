package com.myce.ai.service;

import com.myce.chat.dto.MessageResponse;

/**
 * AI 채팅 서비스
 * 
 * AWS Bedrock Nova Lite를 이용한 플랫폼 AI 상담 서비스
 */
public interface AIChatService {

    /**
     * AI 응답 생성
     * 
     * @param userMessage 사용자 메시지
     * @param roomCode 채팅방 코드 (컨텍스트용)
     * @return AI 응답 메시지
     */
    String generateAIResponse(String userMessage, String roomCode);

    /**
     * AI 메시지 전송 및 저장
     * 
     * @param roomCode 채팅방 코드
     * @param userMessage 사용자 원본 메시지
     * @return AI 응답 메시지 정보
     */
    MessageResponse sendAIMessage(String roomCode, String userMessage);

    /**
     * AI 활성화 상태 확인
     * 
     * @param roomCode 채팅방 코드
     * @return AI 활성화 여부 (platform- 방만 true)
     */
    boolean isAIEnabled(String roomCode);

    /**
     * AI에서 관리자로 상담 이관
     * 
     * @param roomCode 채팅방 코드
     * @param adminCode 담당 관리자 코드
     */
    void handoffToAdmin(String roomCode, String adminCode);
    
    /**
     * AI 상담 요약 생성 (관리자 인계용)
     * 
     * @param roomCode 채팅방 코드
     * @return 대화 요약 텍스트
     */
    String generateConversationSummary(String roomCode);
    
    /**
     * 관리자 연결 요청 (대기 상태 시작)
     * 
     * @param roomCode 채팅방 코드
     * @return 대기 메시지
     */
    MessageResponse requestAdminHandoff(String roomCode);
    
    /**
     * 관리자 연결 요청 취소
     * 
     * @param roomCode 채팅방 코드
     * @return 취소 확인 메시지
     */
    MessageResponse cancelAdminHandoff(String roomCode);
    
    /**
     * AI로 복귀 요청 (관리자 → AI)
     * 
     * @param roomCode 채팅방 코드
     * @return AI 복귀 메시지
     */
    MessageResponse requestAIReturn(String roomCode);
}