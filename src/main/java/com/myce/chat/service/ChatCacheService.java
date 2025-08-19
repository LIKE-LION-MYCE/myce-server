package com.myce.chat.service;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 채팅 Redis 캐시 서비스 인터페이스
 */
public interface ChatCacheService {

    /**
     * 최근 메시지 캐시 조회
     * @param roomCode 채팅방 코드
     * @param limit 조회할 메시지 개수
     * @return 캐시된 메시지 목록 (캐시 미스 시 null)
     */
    List<ChatMessage> getCachedRecentMessages(String roomCode, int limit);

    /**
     * 최근 메시지 캐싱
     * @param roomCode 채팅방 코드
     * @param messages 캐싱할 메시지 목록
     */
    void cacheRecentMessages(String roomCode, List<ChatMessage> messages);

    /**
     * 새 메시지 캐시에 추가
     * @param roomCode 채팅방 코드
     * @param message 추가할 메시지
     * @return 비동기 처리 결과
     */
    CompletableFuture<Void> addMessageToCache(String roomCode, ChatMessage message);

    /**
     * 미읽음 카운트 증가
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @return 증가 후 카운트
     */
    Long incrementUnreadCount(String roomCode, Long userId);

    /**
     * 미읽음 카운트 리셋
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     */
    void resetUnreadCount(String roomCode, Long userId);

    /**
     * 미읽음 카운트 조회
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @return 미읽음 카운트
     */
    Long getUnreadCount(String roomCode, Long userId);

    /**
     * 전체 배지 카운트 증가
     * @param userId 사용자 ID
     * @return 증가 후 전체 카운트
     */
    Long incrementBadgeCount(Long userId);

    /**
     * 전체 배지 카운트 재계산
     * @param userId 사용자 ID
     * @return 재계산된 전체 카운트
     */
    Long recalculateBadgeCount(Long userId);

    /**
     * 전체 배지 카운트 조회
     * @param userId 사용자 ID
     * @return 전체 배지 카운트
     */
    Long getBadgeCount(Long userId);

    /**
     * 마지막 읽은 메시지 ID 저장
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @param messageId 마지막 읽은 메시지 ID
     */
    void setLastReadMessageId(String roomCode, Long userId, String messageId);

    /**
     * 마지막 읽은 메시지 ID 조회
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @return 마지막 읽은 메시지 ID
     */
    String getLastReadMessageId(String roomCode, Long userId);

    /**
     * 채팅방 캐시 무효화
     * @param roomCode 채팅방 코드
     */
    void invalidateRoomCache(String roomCode);

    /**
     * 사용자별 활성 채팅방 목록 조회
     * @param userId 사용자 ID
     * @return 활성 채팅방 코드 목록
     */
    List<String> getUserActiveRooms(Long userId);

    /**
     * 사용자 활성 채팅방 추가
     * @param userId 사용자 ID
     * @param roomCode 채팅방 코드
     */
    void addUserActiveRoom(Long userId, String roomCode);

    /**
     * ChatRoom 캐시 조회
     * @param roomCode 채팅방 코드
     * @return 캐시된 ChatRoom (캐시 미스 시 null)
     */
    ChatRoom getCachedChatRoom(String roomCode);

    /**
     * ChatRoom 캐싱
     * @param roomCode 채팅방 코드
     * @param chatRoom 캐싱할 ChatRoom
     */
    void cacheChatRoom(String roomCode, ChatRoom chatRoom);
}