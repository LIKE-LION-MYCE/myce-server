package com.myce.chat.service.impl;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.type.MessageSenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 플랫폼 채팅방 상태 관리 전담 서비스
 *
 * 책임:
 * 1. 플랫폼 채팅방 상태 동기화 (AI_ACTIVE, WAITING_FOR_ADMIN, ADMIN_ACTIVE)
 * 2. 플랫폼 채팅방 자동 생성 (사용자당 1개만)
 * 3. 채팅방 상태를 메시지 기록에서 재구성
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformChatStateManager {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCacheService chatCacheService;

    /**
     * 플랫폼 채팅방의 상태를 동기화합니다.
     * 저장된 상태와 메시지 기록을 비교하여 정확한 상태를 결정합니다.
     *
     * @param chatRoom 동기화할 채팅방
     */
    @Transactional
    public void syncPlatformChatState(ChatRoom chatRoom) {
        if (!isPlatformRoom(chatRoom)) {
            return;
        }

        ChatRoom.ChatRoomState determinedState = determinePlatformChatState(chatRoom);

        if (!determinedState.equals(chatRoom.getCurrentState())) {
            chatRoom.transitionToState(determinedState);
            ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
            chatCacheService.cacheChatRoom(chatRoom.getRoomCode(), savedRoom);

            log.info("Platform chat state synced - roomCode: {}, newState: {}",
                chatRoom.getRoomCode(), determinedState);
        }
    }

    /**
     * 플랫폼 채팅방의 현재 상태를 결정합니다.
     * 1. 저장된 상태가 있으면 우선 사용
     * 2. 없으면 메시지 기록에서 상태 유추
     *
     * @param chatRoom 상태를 결정할 채팅방
     * @return 결정된 상태
     */
    private ChatRoom.ChatRoomState determinePlatformChatState(ChatRoom chatRoom) {
        // 저장된 상태가 명확하면 사용
        if (chatRoom.getCurrentState() != null &&
            !chatRoom.getCurrentState().equals(ChatRoom.ChatRoomState.AI_ACTIVE)) {
            return chatRoom.getCurrentState();
        }

        // 저장된 상태가 없으면 메시지 기록에서 추론
        List<ChatMessage> recentMessages = chatMessageRepository
            .findTop50ByRoomCodeOrderBySentAtDesc(chatRoom.getRoomCode());

        // 관리자 메시지 개수 계산 (SYSTEM 제외)
        long realAdminMessageCount = recentMessages.stream()
            .filter(msg -> MessageSenderType.ADMIN.name().equals(msg.getSenderType()))
            .count();

        // 관리자 메시지가 있으면 ADMIN_ACTIVE, 없으면 AI_ACTIVE
        if (realAdminMessageCount > 0) {
            return ChatRoom.ChatRoomState.ADMIN_ACTIVE;
        }

        return ChatRoom.ChatRoomState.AI_ACTIVE;
    }

    /**
     * 플랫폼 채팅방의 존재 여부를 확인하고 없으면 생성합니다.
     *
     * @param memberId 사용자 ID
     * @param memberName 사용자 이름
     */
    @Transactional
    public void ensurePlatformRoomExists(Long memberId, String memberName) {
        String roomCode = "platform-" + memberId;

        boolean roomExists = chatRoomRepository.findByRoomCode(roomCode).isPresent();

        if (!roomExists) {
            ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .memberId(memberId)
                .memberName(memberName)
                .build();

            // 플랫폼 채팅방은 기본적으로 AI_ACTIVE 상태로 시작
            // (생성자에서 자동으로 설정되지만 명시적으로 보장)
            newRoom.transitionToState(ChatRoom.ChatRoomState.AI_ACTIVE);

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            chatCacheService.cacheChatRoom(roomCode, savedRoom);

            log.info("Platform chat room created - roomCode: {}, memberId: {}",
                roomCode, memberId);
        }
    }

    /**
     * 플랫폼 방 여부를 확인합니다.
     *
     * @param chatRoom 확인할 채팅방
     * @return 플랫폼 방이면 true
     */
    private boolean isPlatformRoom(ChatRoom chatRoom) {
        return chatRoom.getExpoId() == null &&
               chatRoom.getRoomCode() != null &&
               chatRoom.getRoomCode().startsWith("platform-");
    }
}
