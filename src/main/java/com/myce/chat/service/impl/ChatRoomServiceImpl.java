package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatRoomService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.ai.service.AIChatService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.util.ChatReadStatusUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 채팅방 서비스 구현체 (파사드 패턴)
 *
 * 책임:
 * 1. ChatRoomService 인터페이스 구현 (외부 호출 지점)
 * 2. 세부 로직은 내부 서비스들로 위임
 *   - ChatRoomQueryService: 채팅방 조회
 *   - ChatRoomAccessValidator: 권한 검증
 *   - PlatformChatStateManager: 플랫폼 채팅 상태 관리
 *
 * 주요 메서드:
 * - getChatRooms(): 사용자 채팅방 목록
 * - getChatRoomsByExpo(): 박람회 채팅방 목록
 * - markAsRead(): 읽음 처리
 * - handoffAIToAdmin(): AI 핸드오프
 * - getUnreadCount(): 미읽음 수
 * - validateChatRoomAccess(): 권한 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl implements ChatRoomService {

    // 세부 로직 처리 서비스들
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomAccessValidator accessValidator;
    private final PlatformChatStateManager stateManager;

    // 기본 리포지토리
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    // 외부 서비스
    private final AIChatService aiChatService;
    private final ChatUnreadService chatUnreadService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 현재 로그인한 사용자의 채팅방 목록 조회
     */
    @Override
    public ChatRoomListResponse getChatRooms(Long memberId, String memberRole) {
        // 회원 존재 여부 확인
        Member currentMember = memberRepository.findById(memberId)
            .orElseThrow(() -> {
                log.error("Member not found - memberId: {}", memberId);
                return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
            });

        // 플랫폼 채팅방 자동 생성
        stateManager.ensurePlatformRoomExists(memberId, currentMember.getName());

        // ChatRoomQueryService로 위임
        return chatRoomQueryService.getChatRoomsForUser(memberId, memberRole);
    }

    /**
     * 특정 박람회의 채팅방 목록 조회 (관리자 전용)
     */
    @Override
    public ChatRoomListResponse getChatRoomsByExpo(Long expoId, Long adminId) {
        // ChatRoomQueryService로 위임
        return chatRoomQueryService.getChatRoomsForExpo(expoId, adminId);
    }

    /**
     * 채팅방 읽음 처리
     */
    @Override
    @Transactional
    public void markAsRead(String roomCode, String lastReadMessageId, Long memberId, String memberRole) {
        // 권한 검증
        validateChatRoomAccess(roomCode, memberId, memberRole);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));

        // 읽음 상태 업데이트 - 역할에 따라 다른 메서드 호출
        String updatedReadStatus;
        if ("ADMIN".equals(memberRole)) {
            updatedReadStatus = ChatReadStatusUtil.updateReadStatusForAdmin(
                chatRoom.getReadStatusJson(), lastReadMessageId
            );
        } else {
            updatedReadStatus = ChatReadStatusUtil.updateReadStatusForUser(
                chatRoom.getReadStatusJson(), lastReadMessageId
            );
        }
        // ChatRoom의 updateReadStatus 메서드 사용 (updatedAt도 함께 업데이트)
        chatRoom.updateReadStatus(updatedReadStatus);
        chatRoomRepository.save(chatRoom);

        log.debug("Chat marked as read - roomCode: {}, userId: {}, lastReadMessageId: {}",
            roomCode, memberId, lastReadMessageId);
    }

    /**
     * AI 상담을 관리자에게 인계
     */
    @Override
    @Transactional
    public void handoffAIToAdmin(String roomCode, String adminCode) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));

        // AI 서비스에 위임하여 관리자로 핸드오프
        aiChatService.handoffToAdmin(roomCode, adminCode);

        log.info("AI handoff completed - roomCode: {}, adminCode: {}", roomCode, adminCode);
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    @Override
    public Long getUnreadCount(String roomCode, Long memberId, String memberRole) {
        // 권한 검증
        validateChatRoomAccess(roomCode, memberId, memberRole);

        // ChatUnreadService로 위임
        return chatUnreadService.getUnreadCountForViewer(roomCode, memberId, memberRole);
    }

    /**
     * 채팅방 접근 권한 검증
     */
    @Override
    public void validateChatRoomAccess(String roomCode, Long memberId, String memberRole) {
        // ChatRoomAccessValidator로 위임
        accessValidator.validateAccess(roomCode, memberId, memberRole);
    }
}
