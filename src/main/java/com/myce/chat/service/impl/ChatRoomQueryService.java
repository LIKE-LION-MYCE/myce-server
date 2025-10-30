package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.mapper.ChatRoomMapper;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 전담 서비스
 *
 * 책임:
 * 1. 권한별 채팅방 목록 조회 (플랫폼 관리자 vs 일반 사용자)
 * 2. 채팅방 정보를 DTO로 변환
 * 3. 미읽음 메시지 개수 계산 및 반영
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatCacheService chatCacheService;
    private final ChatUnreadService chatUnreadService;

    /**
     * 사용자의 채팅방 목록을 조회합니다.
     * 역할에 따라 다른 쿼리를 실행합니다.
     *
     * @param memberId 현재 사용자 ID
     * @param memberRole 사용자 역할 (PLATFORM_ADMIN, USER, EXPO_ADMIN)
     * @return 채팅방 목록 응답
     */
    public ChatRoomListResponse getChatRoomsForUser(Long memberId, String memberRole) {
        List<ChatRoom> chatRooms;

        if (Role.PLATFORM_ADMIN.name().equals(memberRole)) {
            // 플랫폼 관리자: 모든 플랫폼 채팅방 조회
            chatRooms = chatRoomRepository.findByExpoIdIsNullAndIsActiveTrueOrderByLastMessageAtDesc();
        } else {
            // 일반 사용자, EXPO_ADMIN: 본인이 참여한 채팅방만 조회
            chatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(memberId);
        }

        return convertToResponse(chatRooms, memberId, memberRole);
    }

    /**
     * 특정 박람회의 채팅방 목록을 관리자용으로 조회합니다.
     *
     * @param expoId 박람회 ID
     * @param adminId 관리자 ID
     * @return 채팅방 목록 응답
     */
    public ChatRoomListResponse getChatRoomsForExpo(Long expoId, Long adminId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(expoId);
        return convertToResponse(chatRooms, adminId, Role.EXPO_ADMIN.name());
    }

    /**
     * ChatRoom 도큐먼트 목록을 DTO 응답으로 변환합니다.
     *
     * @param chatRooms MongoDB의 ChatRoom 목록
     * @param viewerId 조회하는 사용자 ID
     * @param viewerRole 조회하는 사용자 역할
     * @return 변환된 응답 객체
     */
    private ChatRoomListResponse convertToResponse(List<ChatRoom> chatRooms, Long viewerId, String viewerRole) {
        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfos = chatRooms.stream()
            .map(chatRoom -> {
                try {
                    return convertToChatRoomInfo(chatRoom, viewerId, viewerRole);
                } catch (Exception e) {
                    log.error("Failed to convert chat room to DTO - chatRoom ID: {}, error: {}",
                        chatRoom.getId(), e.getMessage(), e);
                    return null;
                }
            })
            .filter(info -> info != null)
            .collect(Collectors.toList());

        return ChatRoomListResponse.builder()
            .chatRooms(chatRoomInfos)
            .totalCount(chatRoomInfos.size())
            .build();
    }

    /**
     * 개별 ChatRoom을 ChatRoomInfo DTO로 변환합니다.
     * 이 과정에서 미읽음 메시지 개수를 계산하고 상대방 정보를 매핑합니다.
     *
     * @param chatRoom 변환할 ChatRoom 도큐먼트
     * @param viewerId 조회하는 사용자 ID
     * @param viewerRole 조회하는 사용자 역할
     * @return 변환된 ChatRoomInfo
     */
    private ChatRoomListResponse.ChatRoomInfo convertToChatRoomInfo(ChatRoom chatRoom, Long viewerId, String viewerRole) {
        // 미읽음 메시지 개수 계산
        int unreadCount = chatUnreadService.getUnreadCountForViewer(
            chatRoom.getRoomCode(), viewerId, viewerRole
        ).intValue();

        // 상대방(채팅방 생성자) 정보
        Long otherMemberId = chatRoom.getMemberId();
        String otherMemberName = chatRoom.getMemberName();
        String otherMemberRole = Role.USER.name(); // 기본값은 USER

        // 박람회 정보 (필요시 조회)
        String expoTitle = null;
        if (chatRoom.getExpoId() != null) {
            expoTitle = chatRoom.getExpoTitle() != null ?
                chatRoom.getExpoTitle() : "박람회";
        }

        // Mapper를 통한 변환
        return ChatRoomMapper.toDto(
            chatRoom,
            otherMemberId,
            otherMemberName,
            otherMemberRole,
            expoTitle,
            unreadCount
        );
    }
}
