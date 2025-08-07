package com.myce.chat.repository;

import com.myce.chat.document.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 MongoDB Repository
 * 
 * 주요 기능:
 * 1. 사용자별 채팅방 목록 조회 (권한 기반)
 * 2. 박람회별 채팅방 조회 (관리자용)
 * 3. roomCode 기반 채팅방 검색
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    /**
     * 채팅방 코드로 단일 채팅방 조회
     */
    Optional<ChatRoom> findByRoomCode(String roomCode);

    /**
     * 특정 회원이 참여한 활성화된 채팅방 목록 조회 (사용자용)
     */
    @Query(value = "{ 'memberId': ?0, 'isActive': true }", sort = "{ 'lastMessageAt': -1 }")
    List<ChatRoom> findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(Long memberId);

    /**
     * 특정 박람회의 모든 활성화된 채팅방 조회 (관리자용)
     */
    @Query(value = "{ 'expoId': ?0, 'isActive': true }", sort = "{ 'lastMessageAt': -1 }")
    List<ChatRoom> findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(Long expoId);

    /**
     * 특정 박람회의 특정 회원 채팅방 조회 (중복 방지용)
     */
    Optional<ChatRoom> findByExpoIdAndMemberId(Long expoId, Long memberId);

    /**
     * 활성화된 모든 채팅방 개수 조회 (통계용)
     */
    Long countByIsActiveTrue();
}