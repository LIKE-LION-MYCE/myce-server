package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.service.ChatWebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * WebSocket 메시지 브로드캐스트 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWebSocketBroadcasterImpl implements ChatWebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastUserMessage(String roomId, MessageResponse messageResponse, ChatRoom chatRoom) {
        try {
            Map<String, Object> payload = Map.of(
                "roomId", roomId,
                "messageId", messageResponse.getMessageId(),
                "senderId", messageResponse.getSenderId(),
                "senderType", messageResponse.getSenderType(),
                "senderName", messageResponse.getSenderName() != null ? messageResponse.getSenderName() : "사용자",
                "content", messageResponse.getContent(),
                "sentAt", messageResponse.getSentAt().toString()
            );

            Map<String, Object> roomState = createRoomStateInfo(chatRoom, "user_message");

            Map<String, Object> broadcastMessage = Map.of(
                "type", "MESSAGE",
                "payload", payload,
                "roomState", roomState
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, broadcastMessage);
            log.debug("사용자 메시지 브로드캐스트 완료 - roomId: {}, messageId: {}",
                roomId, messageResponse.getMessageId());

        } catch (Exception e) {
            log.error("사용자 메시지 브로드캐스트 실패 - roomId: {}", roomId, e);
        }
    }

    @Override
    public void broadcastAdminMessage(String roomCode, MessageResponse messageResponse, ChatRoom chatRoom, String adminCode) {
        try {
            Map<String, Object> payload = Map.of(
                "roomCode", roomCode,
                "messageId", messageResponse.getMessageId(),
                "senderId", messageResponse.getSenderId(),
                "senderType", "ADMIN",
                "adminCode", adminCode,
                "adminDisplayName", chatRoom.getAdminDisplayName(),
                "content", messageResponse.getContent(),
                "sentAt", messageResponse.getSentAt().toString()
            );

            Map<String, Object> adminRoomState = createRoomStateInfo(chatRoom, "admin_message");

            Map<String, Object> broadcastMessage = Map.of(
                "type", "ADMIN_MESSAGE",
                "payload", payload,
                "roomState", adminRoomState
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomCode, broadcastMessage);
            log.debug("관리자 메시지 브로드캐스트 완료 - roomCode: {}, messageId: {}",
                roomCode, messageResponse.getMessageId());

        } catch (Exception e) {
            log.error("관리자 메시지 브로드캐스트 실패 - roomCode: {}", roomCode, e);
        }
    }

    @Override
    public void broadcastSystemMessage(String roomId, String messageType, Map<String, Object> payload) {
        try {
            Map<String, Object> broadcastMessage = Map.of(
                "type", messageType,
                "payload", payload
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, broadcastMessage);
            log.debug("시스템 메시지 브로드캐스트 완료 - roomId: {}, type: {}", roomId, messageType);

        } catch (Exception e) {
            log.error("시스템 메시지 브로드캐스트 실패 - roomId: {}", roomId, e);
        }
    }

    @Override
    public void broadcastButtonStateUpdate(String roomId, String newState) {
        try {
            Map<String, Object> statePayload = Map.of(
                "roomId", roomId,
                "state", newState,
                "buttonText", getButtonText(newState),
                "buttonAction", getButtonAction(newState)
            );

            Map<String, Object> stateBroadcast = Map.of(
                "type", "BUTTON_STATE_UPDATE",
                "payload", statePayload
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, stateBroadcast);
            log.debug("버튼 상태 업데이트 브로드캐스트 완료 - roomId: {}, state: {}", roomId, newState);

        } catch (Exception e) {
            log.warn("버튼 상태 업데이트 전송 실패 - roomId: {}, state: {}", roomId, newState, e);
        }
    }

    @Override
    public void broadcastReadStatusUpdate(String roomId, String messageId, Long readBy, String readerType) {
        try {
            Map<String, Object> readStatusUpdate = Map.of(
                "type", "read_status_update",
                "messageId", messageId,
                "readBy", readBy,
                "readerType", readerType,
                "timestamp", System.currentTimeMillis()
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, readStatusUpdate);
            log.debug("읽음 상태 브로드캐스트 완료 - roomId: {}, messageId: {}", roomId, messageId);

        } catch (Exception e) {
            log.error("읽음 상태 브로드캐스트 실패 - roomId: {}", roomId, e);
        }
    }

    @Override
    public void broadcastUnreadCountUpdate(Long expoId, String roomCode, Long unreadCount) {
        try {
            Map<String, Object> unreadUpdatePayload = Map.of(
                "roomCode", roomCode,
                "unreadCount", unreadCount
            );

            Map<String, Object> unreadUpdateMessage = Map.of(
                "type", "unread_count_update",
                "payload", unreadUpdatePayload
            );

            messagingTemplate.convertAndSend(
                "/topic/expo/" + expoId + "/chat-room-updates",
                unreadUpdateMessage
            );
            log.debug("미읽음 카운트 업데이트 브로드캐스트 완료 - expoId: {}, roomCode: {}, unreadCount: {}",
                expoId, roomCode, unreadCount);

        } catch (Exception e) {
            log.warn("미읽음 카운트 업데이트 전송 실패 - roomCode: {}", roomCode, e);
        }
    }

    @Override
    public void broadcastAdminAssignment(String roomCode, ChatRoom chatRoom, Long expoId) {
        try {
            Map<String, Object> assignmentPayload = Map.of(
                "roomCode", roomCode,
                "currentAdminCode", chatRoom.getCurrentAdminCode() != null ? chatRoom.getCurrentAdminCode() : "PLATFORM_ADMIN",
                "adminDisplayName", chatRoom.getAdminDisplayName() != null ? chatRoom.getAdminDisplayName() : "플랫폼 관리자"
            );

            Map<String, Object> assignmentMessage = Map.of(
                "type", "admin_assignment_update",
                "payload", assignmentPayload
            );

            messagingTemplate.convertAndSend("/topic/chat/" + roomCode, assignmentMessage);

            if (expoId != null) {
                messagingTemplate.convertAndSend(
                    "/topic/expo/" + expoId + "/admin-updates",
                    assignmentMessage
                );
            }

            log.debug("담당자 배정 브로드캐스트 완료 - roomCode: {}, adminCode: {}",
                roomCode, chatRoom.getCurrentAdminCode());

        } catch (Exception e) {
            log.error("담당자 배정 브로드캐스트 실패 - roomCode: {}", roomCode, e);
        }
    }

    @Override
    public void broadcastError(String sessionId, Long userId, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", errorMessage
            );

            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", error);
            log.debug("에러 메시지 브로드캐스트 완료 - sessionId: {}, userId: {}", sessionId, userId);

        } catch (Exception e) {
            log.error("에러 메시지 전송 실패: {}", errorMessage, e);
        }
    }

    @Override
    public void broadcastCustomError(String sessionId, String errorCode, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "errorCode", errorCode,
                "message", errorMessage,
                "payload", Map.of(
                    "code", errorCode,
                    "message", errorMessage
                )
            );

            String generalErrorChannel = "/topic/user/errors";
            messagingTemplate.convertAndSend(generalErrorChannel, error);

            String userErrorChannel = "/topic/user/" + sessionId + "/errors";
            messagingTemplate.convertAndSend(userErrorChannel, error);

            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", error);

            log.debug("커스텀 에러 메시지 브로드캐스트 완료 - sessionId: {}, errorCode: {}", sessionId, errorCode);

        } catch (Exception e) {
            log.error("커스텀 에러 메시지 전송 실패 - errorCode: {}", errorCode, e);
        }
    }

    /**
     * 상태별 버튼 텍스트 반환
     */
    private String getButtonText(String state) {
        return switch (state) {
            case "AI_ACTIVE" -> "Request Human";
            case "WAITING_FOR_ADMIN" -> "Cancel Request";
            case "ADMIN_ACTIVE" -> "Request AI";
            default -> "Request Human";
        };
    }

    /**
     * 상태별 버튼 액션 반환
     */
    private String getButtonAction(String state) {
        return switch (state) {
            case "AI_ACTIVE" -> "request_handoff";
            case "WAITING_FOR_ADMIN" -> "cancel_handoff";
            case "ADMIN_ACTIVE" -> "request_ai";
            default -> "request_handoff";
        };
    }

    /**
     * 채팅방 상태 정보 생성 (Controller에서 추출)
     */
    private Map<String, Object> createRoomStateInfo(ChatRoom chatRoom, String transitionReason) {
        if (chatRoom == null) {
            return Map.of(
                "current", "AI_ACTIVE",
                "description", "AI 상담 중",
                "buttonText", "Request Human",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "unknown"
            );
        }

        ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
        Map<String, Object> stateInfo = Map.of(
            "current", currentState.name(),
            "description", currentState.getDescription(),
            "buttonText", currentState.getButtonText(),
            "timestamp", java.time.LocalDateTime.now().toString(),
            "transitionReason", transitionReason != null ? transitionReason : "message_flow"
        );

        // Add admin info for admin active states
        if (currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE && chatRoom.hasAssignedAdmin()) {
            Map<String, Object> adminInfo = Map.of(
                "adminCode", chatRoom.getCurrentAdminCode(),
                "displayName", chatRoom.getAdminDisplayName() != null ? chatRoom.getAdminDisplayName() : "관리자",
                "lastActivity", chatRoom.getLastAdminActivity() != null ? chatRoom.getLastAdminActivity().toString() : ""
            );

            return Map.of(
                "current", currentState.name(),
                "description", currentState.getDescription(),
                "buttonText", currentState.getButtonText(),
                "timestamp", java.time.LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "message_flow",
                "adminInfo", adminInfo
            );
        }


        if (currentState == ChatRoom.ChatRoomState.WAITING_FOR_ADMIN && chatRoom.getHandoffRequestedAt() != null) {
            Map<String, Object> handoffInfo = Map.of(
                "requestedAt", chatRoom.getHandoffRequestedAt().toString(),
                "aiSummaryGenerated", false
            );

            return Map.of(
                "current", currentState.name(),
                "description", currentState.getDescription(),
                "buttonText", currentState.getButtonText(),
                "timestamp", java.time.LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "message_flow",
                "handoffInfo", handoffInfo
            );
        }

        return stateInfo;
    }
}
