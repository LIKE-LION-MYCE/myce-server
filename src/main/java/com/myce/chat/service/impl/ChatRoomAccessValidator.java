package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatRoomAccessCacheService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 채팅방 접근 권한 검증 전담 서비스
 *
 * 책임:
 * 1. 플랫폼 채팅방 접근 권한 검증
 * 2. 박람회 채팅방 접근 권한 검증
 * 3. AdminCode 기반 권한 검증
 * 4. 복잡한 권한 검증 로직을 캐싱하여 성능 최적화
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomAccessValidator {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final ChatRoomAccessCacheService accessCacheService;

    /**
     * 사용자가 특정 채팅방에 접근할 권한이 있는지 검증합니다.
     *
     * @param roomCode 채팅방 코드
     * @param memberId 사용자 ID
     * @param memberRole 사용자 역할
     * @throws CustomException 접근 권한이 없는 경우
     */
    public void validateAccess(String roomCode, Long memberId, String memberRole) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> {
                log.warn("Chat room not found - roomCode: {}", roomCode);
                return new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
            });

        // 채팅방 유형에 따라 다른 검증 수행
        if (chatRoom.getExpoId() == null) {
            // 플랫폼 채팅방
            validatePlatformChatAccess(chatRoom, memberId, memberRole);
        } else {
            // 박람회 채팅방
            validateExpoChatAccess(chatRoom, memberId, memberRole);
        }
    }

    /**
     * 플랫폼 채팅방에 대한 접근 권한을 검증합니다.
     *
     * @param chatRoom 플랫폼 채팅방
     * @param memberId 사용자 ID
     * @param memberRole 사용자 역할
     * @throws CustomException 접근 권한이 없는 경우
     */
    private void validatePlatformChatAccess(ChatRoom chatRoom, Long memberId, String memberRole) {
        if (Role.PLATFORM_ADMIN.name().equals(memberRole)) {
            // 플랫폼 관리자는 항상 접근 가능
            log.debug("Platform admin access allowed - roomCode: {}, adminId: {}",
                chatRoom.getRoomCode(), memberId);
            return;
        }

        // 일반 사용자: 본인이 생성한 채팅방만 접근 가능
        if (chatRoom.getMemberId().equals(memberId)) {
            log.debug("User platform chat access allowed - roomCode: {}, userId: {}",
                chatRoom.getRoomCode(), memberId);
            return;
        }

        log.warn("Unauthorized platform chat access attempt - roomCode: {}, userId: {}",
            chatRoom.getRoomCode(), memberId);
        throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    /**
     * 박람회 채팅방에 대한 접근 권한을 검증합니다.
     * AdminCode 기반 권한과 Expo Owner 권한을 모두 확인합니다.
     *
     * @param chatRoom 박람회 채팅방
     * @param memberId 사용자 ID
     * @param memberRole 사용자 역할
     * @throws CustomException 접근 권한이 없는 경우
     */
    private void validateExpoChatAccess(ChatRoom chatRoom, Long memberId, String memberRole) {
        if (Role.EXPO_ADMIN.name().equals(memberRole)) {
            // EXPO_ADMIN: AdminCode 권한 확인
            if (validateAdminCodeAccess(chatRoom, memberId)) {
                log.debug("Expo admin access allowed - roomCode: {}, adminId: {}",
                    chatRoom.getRoomCode(), memberId);
                return;
            }

            // AdminCode 실패 시 Expo Owner 권한 확인
            if (validateExpoOwnerAccess(chatRoom, memberId)) {
                log.debug("Expo owner access allowed - roomCode: {}, ownerId: {}",
                    chatRoom.getRoomCode(), memberId);
                return;
            }

            log.warn("Unauthorized expo admin access - roomCode: {}, adminId: {}",
                chatRoom.getRoomCode(), memberId);
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 일반 사용자: 본인이 생성한 채팅방만 접근 가능
        if (chatRoom.getMemberId().equals(memberId)) {
            log.debug("User expo chat access allowed - roomCode: {}, userId: {}",
                chatRoom.getRoomCode(), memberId);
            return;
        }

        log.warn("Unauthorized expo chat access - roomCode: {}, userId: {}, role: {}",
            chatRoom.getRoomCode(), memberId, memberRole);
        throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    /**
     * 사용자가 관련 AdminCode를 가지고 있는지 확인합니다.
     *
     * @param chatRoom 박람회 채팅방
     * @param memberId 사용자 ID (AdminCode ID인 경우)
     * @return AdminCode 권한이 있으면 true
     */
    private boolean validateAdminCodeAccess(ChatRoom chatRoom, Long memberId) {
        try {
            // AdminCode ID로 조회
            Optional<AdminCode> adminCodeOpt = adminCodeRepository.findById(memberId);

            if (adminCodeOpt.isEmpty()) {
                return false;
            }

            AdminCode adminCode = adminCodeOpt.get();
            // AdminCode의 expoId와 채팅방의 expoId가 일치하고,
            // AdminPermission이 존재하면 활성 상태로 간주
            return adminCode.getExpoId().equals(chatRoom.getExpoId()) &&
                   adminCode.getAdminPermission() != null;
        } catch (Exception e) {
            log.debug("AdminCode validation failed - error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 사용자가 박람회의 owner인지 확인합니다.
     *
     * @param chatRoom 박람회 채팅방
     * @param memberId 사용자 ID
     * @return 박람회 owner면 true
     */
    private boolean validateExpoOwnerAccess(ChatRoom chatRoom, Long memberId) {
        try {
            Optional<Expo> expoOpt = expoRepository.findById(chatRoom.getExpoId());

            if (expoOpt.isEmpty()) {
                log.error("Expo not found - expoId: {}", chatRoom.getExpoId());
                return false;
            }

            Expo expo = expoOpt.get();
            return expo.getMember() != null && expo.getMember().getId().equals(memberId);
        } catch (Exception e) {
            log.error("Expo owner validation failed - error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 사용자의 실제 역할을 채팅방 맥락에서 결정합니다.
     * (향후 더 복잡한 권한 검증이 필요한 경우 확장 가능)
     *
     * @param chatRoom 채팅방
     * @param memberId 사용자 ID
     * @param requestedRole 요청된 역할
     * @return 실제 역할
     */
    public String determineActualRole(ChatRoom chatRoom, Long memberId, String requestedRole) {
        // 플랫폼 채팅방
        if (chatRoom.getExpoId() == null) {
            if (chatRoom.getMemberId().equals(memberId)) {
                return Role.USER.name();
            }
            return requestedRole;
        }

        // 박람회 채팅방
        if (chatRoom.getMemberId().equals(memberId)) {
            return Role.USER.name();
        }

        // AdminCode 확인
        if (validateAdminCodeAccess(chatRoom, memberId)) {
            return Role.EXPO_ADMIN.name();
        }

        // Owner 확인
        if (validateExpoOwnerAccess(chatRoom, memberId)) {
            return Role.EXPO_ADMIN.name();
        }

        return requestedRole;
    }
}
