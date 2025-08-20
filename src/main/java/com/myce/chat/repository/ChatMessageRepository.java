package com.myce.chat.repository;

import com.myce.chat.document.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatMessage MongoDB Repository
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    /**
     * 채팅방별 메시지 조회 (페이징)
     */
    Page<ChatMessage> findByRoomCodeOrderBySentAtDesc(String roomCode, Pageable pageable);
    
    /**
     * 채팅방별 최근 메시지 조회
     */
    List<ChatMessage> findTop50ByRoomCodeOrderBySentAtDesc(String roomCode);
    
    /**
     * 채팅방별 메시지 개수
     */
    long countByRoomCode(String roomCode);
    
    /**
     * 채팅방에서 특정 발송자 타입의 메시지 개수 (안읽은 메시지 계산용)
     */
    long countByRoomCodeAndSenderType(String roomCode, String senderType);
    
    /**
     * 채팅방에서 특정 메시지 ID 이후의 특정 발송자 타입 메시지 개수
     */
    long countByRoomCodeAndSenderTypeAndIdGreaterThan(String roomCode, String senderType, String messageId);
    
    /**
     * 채팅방에서 여러 발송자 타입 중 하나라도 해당하는 메시지 개수 (플랫폼 상태 결정용)
     */
    long countByRoomCodeAndSenderTypeIn(String roomCode, List<String> senderTypes);
}