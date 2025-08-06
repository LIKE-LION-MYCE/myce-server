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
 * 
 * @author MYCE Team
 * @since 2025-08-06
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    /**
     * 채팅방 코드로 단일 채팅방 조회
     * 
     * @param roomCode "admin-{expoId}-{userId}" 형식의 채팅방 코드
     * @return 채팅방 정보 (Optional)
     * 
     * 사용 예시:
     * - 특정 채팅방 존재 여부 확인
     * - 채팅방 정보 상세 조회
     */
    Optional<ChatRoom> findByRoomCode(String roomCode);

    /**
     * 특정 회원이 참여한 활성화된 채팅방 목록 조회 (사용자용)
     * 
     * @param memberId 회원 고유 ID
     * @return 해당 회원의 활성화된 채팅방 목록 (최신 메시지 시간 기준 내림차순)
     * 
     * 비즈니스 로직:
     * - 일반 사용자는 자신이 참여한 채팅방만 볼 수 있음
     * - 비활성화된 채팅방은 제외
     * - 최근 활동 순으로 정렬하여 UX 향상
     */
    @Query("{ 'memberId': ?0, 'isActive': true }")
    List<ChatRoom> findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(Long memberId);

    /**
     * 특정 박람회의 모든 활성화된 채팅방 조회 (관리자용)
     * 
     * @param expoId 박람회 고유 ID
     * @return 해당 박람회의 모든 활성화된 채팅방 목록 (최신 메시지 시간 기준 내림차순)
     * 
     * 비즈니스 로직:
     * - 박람회 관리자는 본인이 관리하는 박람회의 모든 채팅방 확인 가능
     * - 참가자들과의 1:1 채팅방들을 한눈에 관리
     * - 응답이 필요한 채팅방을 우선적으로 확인할 수 있도록 정렬
     */
    @Query("{ 'expoId': ?0, 'isActive': true }")
    List<ChatRoom> findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(Long expoId);

    /**
     * 특정 박람회의 특정 회원 채팅방 조회 (중복 방지용)
     * 
     * @param expoId 박람회 고유 ID  
     * @param memberId 회원 고유 ID
     * @return 해당 조건의 채팅방 (Optional)
     * 
     * 사용 목적:
     * - 동일한 박람회-회원 조합의 채팅방 중복 생성 방지
     * - 채팅방 생성 전 기존 채팅방 존재 여부 확인
     */
    Optional<ChatRoom> findByExpoIdAndMemberId(Long expoId, Long memberId);

    /**
     * 활성화된 모든 채팅방 개수 조회 (통계용)
     * 
     * @return 활성화된 전체 채팅방 개수
     * 
     * 사용 목적:
     * - 시스템 모니터링 및 통계
     * - 대시보드 데이터 제공
     */
    Long countByIsActiveTrue();
}