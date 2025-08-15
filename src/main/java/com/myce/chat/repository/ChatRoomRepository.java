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

    /**
     * 담당자가 배정된 모든 채팅방 조회 (디버깅용)
     */
    @Query("{ 'currentAdminCode': { $ne: null } }")
    List<ChatRoom> findByCurrentAdminCodeIsNotNull();
    
    /**
     * 비활성 담당자가 있는 채팅방 조회 (타임아웃 스케줄러용)
     */
    @Query("{ 'currentAdminCode': { $ne: null }, 'lastAdminActivity': { $lt: ?0 } }")
    List<ChatRoom> findByCurrentAdminCodeIsNotNullAndLastAdminActivityBefore(java.time.LocalDateTime threshold);

    /**
     * 특정 박람회의 모든 채팅방 조회 (active 여부 무관)
     */
    List<ChatRoom> findByExpoId(Long expoId);

    /**
     * 플랫폼 채팅방 조회 (플랫폼 관리자용)
     * expoId가 null인 모든 활성 채팅방 (platform-* rooms)
     */
    @Query(value = "{ 'expoId': null, 'isActive': true }", sort = "{ 'lastMessageAt': -1 }")
    List<ChatRoom> findByExpoIdIsNullAndIsActiveTrueOrderByLastMessageAtDesc();
}