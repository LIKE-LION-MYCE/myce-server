package com.myce.chat.service.impl;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatMessageHandlerService;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.ChatWebSocketBroadcaster;
import com.myce.ai.service.AIChatService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * 메시지 핸들링 구현
 *
 * WebSocket 메시지 수신 후의 복잡한 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageHandlerServiceImpl implements ChatMessageHandlerService {

    private final AIChatService aiChatService;
    private final ChatRoomService chatRoomService;
    private final ChatCacheService chatCacheService;
    private final ChatUnreadService chatUnreadService;
    private final ChatWebSocketBroadcaster broadcaster;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 사용자 메시지 플로우 처리
     * 메시지 수신 후 자동 읽음, AI 응답, 미읽음 카운트 업데이트 등을 처리합니다.
     *
     * @param userId 메시지 발송자 ID
     * @param roomId 채팅방 코드
     * @param content 메시지 내용
     * @param messageResponse 저장된 메시지 응답 객체
     */
    @Override
    @Transactional
    public void handleUserMessageFlow(Long userId, String roomId, String content, MessageResponse messageResponse) {
        try {
            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);

            if (currentRoom != null) {
                // 자동 읽음 처리 (필요시)
                handleAutoReadLogic(userId, roomId, messageResponse.getMessageId(), currentRoom);

                // AI 응답 처리 (필요시)
                handleAIResponse(userId, roomId, content, currentRoom, messageResponse);
            }

            // 미읽음 카운트 업데이트 (박람회 관리자 용)
            handleUnreadCountUpdate(roomId);

        } catch (Exception e) {
            log.error("사용자 메시지 플로우 처리 실패 - roomId: {}, userId: {}", roomId, userId, e);
        }
    }

    @Override
    public MessageResponse handleAIResponse(String roomId, String content) {
        try {
            boolean isAIEnabled = aiChatService.isAIEnabled(roomId);

            if (!isAIEnabled) {
                log.debug("AI 응답 건너뜀 - roomId: {}, AI비활성화", roomId);
                return null;
            }

            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            if (currentRoom == null) {
                log.debug("AI 응답 건너뜀 - roomId: {}, 방 없음", roomId);
                return null;
            }

            ChatRoom.ChatRoomState currentState = currentRoom.getCurrentState();

            // AI는 AI_ACTIVE 또는 WAITING_FOR_ADMIN 상태에서만 응답
            boolean shouldAIRespond = (currentState == ChatRoom.ChatRoomState.AI_ACTIVE ||
                                     currentState == ChatRoom.ChatRoomState.WAITING_FOR_ADMIN);

            if (!shouldAIRespond) {
                log.debug("AI 응답 건너뜀 - roomId: {}, 상태: {}, 이유: AI가 응답할 수 없는 상태",
                    roomId, currentState);
                return null;
            }

            log.debug("AI 응답 처리 시작 - roomId: {}, 현재상태: {}", roomId, currentState);

            MessageResponse aiResponse = aiChatService.sendAIMessage(roomId, content);

            log.debug("AI 응답 생성 완료 - roomId: {}, messageId: {}", roomId, aiResponse.getMessageId());

            return aiResponse;

        } catch (Exception aiError) {
            log.error("AI 응답 처리 실패 - roomId: {}", roomId, aiError);
            return null;
        }
    }

    /**
     * 자동 읽음 처리 로직
     * 플랫폼 채팅방에서 AI 또는 관리자 상담 중일 때 메시지를 자동으로 읽음 처리합니다.
     *
     * @param userId 메시지 발송자 ID
     * @param roomId 채팅방 코드
     * @param messageId 메시지 ID
     * @param currentRoom 현재 채팅방 객체
     */
    @Override
    @Transactional
    public void handleAutoReadLogic(Long userId, String roomId, String messageId, ChatRoom currentRoom) {
        try {
            if (currentRoom == null) {
                return;
            }

            ChatRoom.ChatRoomState currentState = currentRoom.getCurrentState();

            // 플랫폼 채팅방에서 AI_ACTIVE 또는 ADMIN_ACTIVE 상태일 때 자동 읽음 처리
            boolean shouldAutoRead = roomId.startsWith("platform-") &&
                (currentState == ChatRoom.ChatRoomState.AI_ACTIVE ||
                 currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE);

            if (!shouldAutoRead) {
                return;
            }

            try {
                // 발송자 본인 메시지 즉시 읽음 처리
                Member sender = memberRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
                String senderRole = sender.getRole().name();

                chatRoomService.markAsRead(roomId, messageId, userId, senderRole);
                log.debug("플랫폼 상담 중 발송자 본인 메시지 읽음 처리 완료 - userId: {}, roomId: {}, state: {}, messageId: {}",
                          userId, roomId, currentState, messageId);

                // 플랫폼 관리자가 메시지를 보낸 경우 → 해당 유저의 미읽음 메시지도 자동 읽음 처리
                if ("PLATFORM_ADMIN".equals(senderRole) &&
                    currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE) {

                    String[] roomParts = roomId.split("-");
                    Long platformUserId = Long.parseLong(roomParts[1]);

                    // 유저의 미읽은 메시지들을 모두 읽음 처리
                    chatRoomService.markAsRead(roomId, messageId, platformUserId, "USER");
                    log.debug("플랫폼 관리자 답장으로 인한 유저 미읽음 자동 처리 완료 - platformUserId: {}, roomId: {}, messageId: {}",
                              platformUserId, roomId, messageId);
                }

                // 읽음 상태 변경을 WebSocket으로 브로드캐스트
                broadcaster.broadcastReadStatusUpdate(roomId, messageId, userId, "USER");

                log.debug("읽음 상태 WebSocket 브로드캐스트 완료 - roomId: {}, messageId: {}", roomId, messageId);

            } catch (Exception e) {
                log.warn("플랫폼 상담 중 발송자 본인 메시지 읽음 처리 실패 - userId: {}, roomId: {}, state: {}, error: {}",
                         userId, roomId, currentState, e.getMessage());
            }
        } catch (Exception e) {
            log.error("자동 읽음 처리 실패 - roomId: {}, userId: {}", roomId, userId, e);
        }
    }

    /**
     * 미읽음 카운트 업데이트
     * 박람회 채팅에서 관리자의 미읽음 메시지 수를 업데이트하고 브로드캐스트합니다.
     *
     * @param roomId 채팅방 코드
     */
    @Override
    public void handleUnreadCountUpdate(String roomId) {
        try {
            Long expoId = extractExpoIdFromRoomCode(roomId);
            if (expoId == null) {
                return;
            }

            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            if (chatRoom == null) {
                return;
            }

            // 관리자가 마지막으로 읽은 메시지 이후의 USER 메시지만 계산
            Long unreadCount = chatUnreadService.getUnreadCountForViewer(roomId, 0L, "EXPO_ADMIN");

            broadcaster.broadcastUnreadCountUpdate(expoId, roomId, unreadCount);

            log.debug("미읽음 카운트 업데이트 완료 - roomId: {}, expoId: {}, unreadCount: {}",
                roomId, expoId, unreadCount);

        } catch (Exception unreadUpdateError) {
            log.warn("미읽음 카운트 업데이트 처리 실패 - roomId: {}, error: {}",
                roomId, unreadUpdateError.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleAdminStateTransition(ChatRoom chatRoom, String adminCode, Long userId, Map<String, Object> errorHandler) {
        try {
            String roomCode = chatRoom.getRoomCode();
            ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();

            log.debug("관리자 상태 전환 처리 시작 - roomCode: {}, currentState: {}, adminCode: {}",
                roomCode, currentState, adminCode);

            // 플랫폼 방일 때만 상태별 처리
            if (!roomCode.startsWith("platform-")) {
                log.debug("비플랫폼 방이므로 상태 전환 건너뜀 - roomCode: {}", roomCode);
                return;
            }

            switch (currentState) {
                case WAITING_FOR_ADMIN -> {
                    // AI 핸드오프 시스템으로 요약 및 상태 전환
                    try {
                        chatRoomService.handoffAIToAdmin(roomCode, adminCode);
                        ChatRoom refreshedRoom = chatRoomRepository.findByRoomCode(roomCode)
                            .orElse(null);
                        if (refreshedRoom != null) {
                            log.info("AI 핸드오프 완료 - roomCode: {}, adminCode: {}, newState: {}",
                                roomCode, adminCode, refreshedRoom.getCurrentState());
                        }
                    } catch (Exception handoffError) {
                        log.error("AI 핸드오프 실패 - roomCode: {}, adminCode: {}", roomCode, adminCode, handoffError);
                    }
                }

                case AI_ACTIVE -> {
                    // AI 활성 상태에서 직접 메시지 차단
                    log.warn("AI_ACTIVE 상태에서 직접 관리자 메시지 차단 - roomCode: {}, adminCode: {}",
                        roomCode, adminCode);

                    if (errorHandler != null && errorHandler.containsKey("sessionId")) {
                        String sessionId = (String) errorHandler.get("sessionId");
                        Map<String, Object> errorPayload = Map.of(
                            "error", "INTERVENTION_REQUIRED",
                            "message", "AI 상담 중에는 직접 메시지를 보낼 수 없습니다. '개입하기' 버튼을 사용해주세요.",
                            "suggestedAction", "USE_INTERVENTION_BUTTON"
                        );
                        messagingTemplate.convertAndSendToUser(
                            sessionId,
                            "/queue/errors",
                            errorPayload
                        );
                    }

                    throw new IllegalStateException("AI_ACTIVE 상태에서는 직접 메시지를 보낼 수 없습니다");
                }

                case ADMIN_ACTIVE -> {
                    // 관리자 이미 활성 - 활동 시간만 업데이트
                    chatRoom.updateAdminActivity();
                    ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
                    chatCacheService.cacheChatRoom(roomCode, savedRoom);
                    log.debug("관리자 활동 시간 업데이트 - roomCode: {}, state: {}", roomCode, currentState);
                }

                default -> {
                    log.debug("상태 전환 처리 안함 - roomCode: {}, state: {}", roomCode, currentState);
                }
            }

        } catch (Exception e) {
            log.error("관리자 상태 전환 처리 실패 - roomCode: {}, adminCode: {}",
                chatRoom.getRoomCode(), adminCode, e);
            throw e;
        }
    }

    /**
     * 관리자 권한 검증
     * 메시지 발송 관리자가 해당 채팅방에 대한 권한을 가지고 있는지 검증합니다.
     *
     * @param chatRoom 대상 채팅방 객체
     * @param adminCode 관리자 코드
     * @param userId 요청 사용자 ID
     * @param errorHandler 에러 응답 전송 핸들러
     * @return 관리자 권한 보유 여부
     */
    @Override
    public boolean validateAdminPermission(ChatRoom chatRoom, String adminCode, Long userId, Map<String, Object> errorHandler) {
        try {
            if (!chatRoom.hasAssignedAdmin()) {
                return true; // 아직 배정 안됨 - 허용
            }

            if (!chatRoom.hasAdminPermission(adminCode)) {
                String errorMsg = String.format("상담 권한이 없습니다. 현재 담당자: %s",
                                                chatRoom.getAdminDisplayName());

                if (errorHandler != null && errorHandler.containsKey("sessionId")) {
                    String sessionId = (String) errorHandler.get("sessionId");
                    Map<String, Object> errorPayload = Map.of(
                        "error", "PERMISSION_DENIED",
                        "message", errorMsg,
                        "currentAdmin", chatRoom.getAdminDisplayName()
                    );
                    messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/errors",
                        errorPayload
                    );
                }

                return false; // 권한 없음
            }

            return true; // 권한 있음

        } catch (Exception e) {
            log.error("관리자 권한 검증 실패 - userId: {}, adminCode: {}", userId, adminCode, e);
            return false;
        }
    }

    /**
     * 룸 코드에서 박람회 ID 추출
     * roomCode 형식: admin-{expoId}-{userId}
     */
    private Long extractExpoIdFromRoomCode(String roomCode) {
        try {
            if (roomCode != null && roomCode.startsWith("admin-")) {
                String[] parts = roomCode.split("-");
                if (parts.length >= 3) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("잘못된 방 코드 형식 - roomCode: {}", roomCode);
        }
        return null;
    }

    /**
     * AI 응답 처리 (내부용)
     */
    private void handleAIResponse(Long userId, String roomId, String content, ChatRoom currentRoom, MessageResponse userMessage) {
        try {
            MessageResponse aiResponse = handleAIResponse(roomId, content);

            if (aiResponse != null) {
                Map<String, Object> aiPayload = Map.of(
                    "roomId", roomId,
                    "messageId", aiResponse.getMessageId(),
                    "senderId", aiResponse.getSenderId(),
                    "senderType", "AI",
                    "content", aiResponse.getContent(),
                    "sentAt", aiResponse.getSentAt().toString()
                );

                Map<String, Object> broadcastMessage = Map.of(
                    "type", "AI_MESSAGE",
                    "payload", aiPayload
                );

                messagingTemplate.convertAndSend(
                    "/topic/chat/" + roomId,
                    broadcastMessage
                );

                log.debug("AI 메시지 브로드캐스트 완료 - roomId: {}", roomId);
            }

        } catch (Exception aiError) {
            log.error("AI 응답 처리 중 에러 발생 - roomId: {}", roomId, aiError);
        }
    }
}
