package com.myce.chat.controller;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.*;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.chat.service.ChatWebSocketService;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.ChatWebSocketBroadcaster;
import com.myce.chat.service.ChatMessageHandlerService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.WebSocketMessageType;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.CustomErrorCode;
import com.myce.ai.service.AIChatService;
import com.myce.auth.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

/**
 * WebSocket STOMP 메시지 핸들러
 * 
 * CRM-189 WebSocket 실시간 메시지 송수신
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final String ADMIN_ROOM_PREFIX = "admin-";
    private static final String ROOM_DELIMITER = "-";
    private static final String ADMIN_CODE_TYPE = "ADMIN_CODE";
    private static final String USER_ERROR_TOPIC_PREFIX = "/topic/user/";
    private static final String ERROR_CHANNEL_SUFFIX = "/errors";

    private final ChatWebSocketService chatWebSocketService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AIChatService aiChatService;
    private final ChatRoomService chatRoomService;
    private final ChatCacheService chatCacheService;
    private final ChatUnreadService chatUnreadService;
    private final JwtUtil jwtUtil;
    private final ChatWebSocketBroadcaster broadcaster;
    private final ChatMessageHandlerService messageHandler;

    /**
     * 인증 처리
     * /app/auth -> JWT 토큰 검증 -> 세션에 사용자 ID 저장
     */
    @MessageMapping("/auth")
    public void authenticate(@Payload Map<String, Object> message, 
                           SimpMessageHeaderAccessor headerAccessor) {
        log.debug("🔐 WebSocket 인증 요청 수신: {}", message);
        try {
            String token = (String) message.get("token");
            Long userId = chatWebSocketService.authenticateUser(token);
            
            headerAccessor.getSessionAttributes().put("userId", userId);
            headerAccessor.getSessionAttributes().put("token", token);
            
            String sessionId = headerAccessor.getSessionId();
            Map<String, Object> authResponse = Map.of(
                "type", "AUTH_ACK",
                "payload", "Authentication successful",
                "userId", userId,
                "sessionId", sessionId
            );
            
            // Send auth response to shared topic
            messagingTemplate.convertAndSend("/topic/auth-test", authResponse);
            
        } catch (Exception e) {
            log.error("WebSocket 인증 실패", e);
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "Authentication failed: " + e.getMessage()
            );
            
            // Send error to shared topic  
            messagingTemplate.convertAndSend("/topic/auth-test", error);
        }
    }

    /**
     * 채팅방 입장
     * /app/join -> 권한 검증 -> 채팅방 입장 -> 새션에 현재 방 저장
     */
    @MessageMapping("/join")
    public void joinRoom(@Payload Map<String, Object> message,
                        SimpMessageHeaderAccessor headerAccessor) {
        log.debug("🚪 WebSocket 방 입장 요청 수신: {}", message);
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            String token = (String) headerAccessor.getSessionAttributes().get("token");
            
            if (userId == null || token == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            chatWebSocketService.joinRoom(userId, roomId, token);
            headerAccessor.getSessionAttributes().put("currentRoomId", roomId);
            
        } catch (Exception e) {
            log.error("채팅방 입장 실패 - roomId: {}", message.get("roomId"));
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "Join room failed: " + e.getMessage()
            );
                    
            String sessionId = headerAccessor.getSessionId();
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors", 
                error
            );
        }
    }

    /**
     * 메시지 전송
     * /app/chat.send -> 메세지 저장 -> 채팅창 구독자들에게 실시간 브로드캐스트
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> message,
                          SimpMessageHeaderAccessor headerAccessor) {
        Long userId = null;
        try {
            userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            String token = (String) headerAccessor.getSessionAttributes().get("token");

            if (userId == null || token == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }

            String roomId = (String) message.get("roomId");
            String content = (String) message.get("message");

            log.debug("메시지 전송 - userId: {}, roomId: {}", userId, roomId);

            // 1. 메시지 저장
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                userId, roomId, content, token
            );

            // 2. 사용자 메시지 브로드캐스트
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            broadcaster.broadcastUserMessage(roomId, messageResponse, chatRoom);

            // 3. 사용자 메시지 플로우 처리 (AI 응답, 자동 읽음, 미읽음 업데이트)
            messageHandler.handleUserMessageFlow(userId, roomId, content, messageResponse);

        } catch (Exception e) {
            log.error("메시지 전송 실패 - roomId: {}, userId: {}, error: {}",
                     message.get("roomId"), userId, e.getMessage(), e);
            broadcaster.broadcastError(headerAccessor.getSessionId(), userId, "Send message failed: " + e.getMessage());
        }
    }

    /**
     * 관리자 채팅 메시지 전송
     * /app/admin/chat.send -> 관리자 권한 검증 -> 담당자 배정 -> 메시지 저장 및 브로드캐스트
     */
    @MessageMapping("/admin/chat.send")
    public void sendAdminMessage(@Payload Map<String, Object> message,
                                SimpMessageHeaderAccessor headerAccessor) {
        Long userId = null;
        try {
            userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }

            String roomCode = (String) message.get("roomCode");
            String content = (String) message.get("message");
            Long expoId = message.get("expoId") != null ? ((Number) message.get("expoId")).longValue() : null;

            log.debug("관리자 메시지 전송 - userId: {}, roomCode: {}", userId, roomCode);

            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));

            // 1. 관리자 코드 결정
            String adminCode = determineAdminCode(roomCode, userId, headerAccessor);

            // 2. 박람회 방에 대한 담당자 배정
            if (!roomCode.startsWith("platform-")) {
                chatWebSocketService.assignAdminIfNeeded(chatRoom, adminCode);
            }

            // 3. 권한 검증
            Map<String, Object> errorHandler = Map.of("sessionId", headerAccessor.getSessionId());
            if (!messageHandler.validateAdminPermission(chatRoom, adminCode, userId, errorHandler)) {
                return; // 권한 없음 - 이미 에러 메시지 전송됨
            }

            // 4. 상태별 처리 (상태 전환, 에러 처리 등)
            messageHandler.handleAdminStateTransition(chatRoom, adminCode, userId, errorHandler);

            // 새로운 chatRoom 데이터 재조회 (상태 변경 가능성)
            chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));

            // 5. 담당자 배정 브로드캐스트
            if (chatRoom.hasAssignedAdmin()) {
                broadcaster.broadcastAdminAssignment(roomCode, chatRoom, expoId);
            }

            // 6. 메시지 저장
            String token = (String) headerAccessor.getSessionAttributes().get("token");
            MessageResponse messageResponse = chatWebSocketService.sendMessage(userId, roomCode, content, token);

            // 7. 관리자 메시지 브로드캐스트
            broadcaster.broadcastAdminMessage(roomCode, messageResponse, chatRoom, adminCode);

        } catch (CustomException e) {
            log.error("관리자 메시지 전송 실패 (CustomException) - roomCode: {}, error: {}",
                message.get("roomCode"), e.getMessage());
            broadcaster.broadcastCustomError(
                headerAccessor.getSessionId(),
                e.getErrorCode().getErrorCode(),
                e.getErrorCode().getMessage()
            );

        } catch (Exception e) {
            log.error("관리자 메시지 전송 실패 - roomCode: {}, userId: {}, error: {}",
                message.get("roomCode"), userId, e.getMessage(), e);
            broadcaster.broadcastError(headerAccessor.getSessionId(), userId, "메시지 전송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 읽음 상태 알림 처리
     * /app/read-status-notify -> 관리자에게 읽음 상태 알림 브로드캐스트
     */
    @MessageMapping("/read-status-notify")
    public void notifyReadStatus(@Payload Map<String, Object> message,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            String readerType = (String) message.get("readerType");
            
            Map<String, Object> payload = Map.of(
                "roomCode", roomId,
                "readerType", readerType,
                "unreadCount", 0
            );
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "read_status_update",
                "payload", payload
            );
            
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                broadcastMessage
            );
            
        } catch (Exception e) {
            log.error("읽음 상태 알림 처리 실패 - roomId: {}", message.get("roomId"));
        }
    }

    /**
     * 관리자 연결 요청 (버튼 액션)
     * /app/request-handoff -> AI가 관리자 연결 대기 상태로 전환
     */


    @MessageMapping("/request-handoff")
    public void requestHandoff(@Payload Map<String, Object> message,
                              SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            
            log.warn(" DEBUG HANDOFF REQUEST - roomId: {}, userId: {}, sessionId: {}", 
                roomId, userId, headerAccessor.getSessionId());
            
            // AI 서비스를 통한 핸드오프 요청 처리
            MessageResponse handoffResponse = aiChatService.requestAdminHandoff(roomId);
            
            // 핸드오프 요청 메시지 브로드캐스트
            Map<String, Object> handoffPayload = Map.of(
                "roomId", roomId,
                "messageId", handoffResponse.getMessageId(),
                "senderId", handoffResponse.getSenderId(),
                "senderType", "AI",
                "content", handoffResponse.getContent(),
                "sentAt", handoffResponse.getSentAt().toString()
            );
            
            // Add room state for handoff request
            ChatRoom handoffRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            Map<String, Object> handoffRoomState = createRoomStateInfo(handoffRoom, "handoff_requested");
            
            Map<String, Object> handoffBroadcast = Map.of(
                "type", "AI_HANDOFF_REQUEST",
                "payload", handoffPayload,
                "roomState", handoffRoomState
            );
            
            String topicChannel = "/topic/chat/" + roomId;
            log.warn(" DEBUG: Sending AI_HANDOFF_REQUEST to channel: {}", topicChannel);
            log.warn(" DEBUG: Message payload: {}", handoffBroadcast);
            
            messagingTemplate.convertAndSend(topicChannel, handoffBroadcast);
            
            log.warn(" DEBUG: AI_HANDOFF_REQUEST sent successfully");
            
            //  플랫폼 채팅방인 경우 플랫폼 관리자에게도 알림
            if (roomId.startsWith("platform-")) {
                Map<String, Object> adminNotification = Map.of(
                    "type", "PLATFORM_HANDOFF_REQUEST",
                    "roomCode", roomId,
                    "userId", userId,
                    "userName", handoffRoom != null ? handoffRoom.getMemberName() : "사용자",
                    "roomState", handoffRoomState,
                    "timestamp", System.currentTimeMillis()
                );
                
                messagingTemplate.convertAndSend("/topic/platform/admin-updates", adminNotification);
                log.info("🔔 플랫폼 관리자에게 상담원 연결 요청 알림 전송 완료 - roomId: {}", roomId);
            }
            
            // 버튼 상태 업데이트 브로드캐스트
            log.warn(" DEBUG: Sending BUTTON_STATE_UPDATE to channel: {}", topicChannel);
            sendButtonStateUpdate(roomId, "WAITING_FOR_ADMIN");
            
            log.info("핸드오프 요청 처리 완료 - roomId: {}, userId: {}", roomId, userId);
            
        } catch (Exception e) {
            log.error("핸드오프 요청 처리 실패 - roomId: {}", message.get("roomId"), e);
            sendErrorMessage(headerAccessor, "핸드오프 요청에 실패했습니다.");
        }
    }

    /**
     * 관리자 연결 요청 취소 (버튼 액션)
     * /app/cancel-handoff -> AI가 일반 상태로 복귀
     */
    @MessageMapping("/cancel-handoff")
    public void cancelHandoff(@Payload Map<String, Object> message,
                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            
            // AI 서비스를 통한 핸드오프 취소 처리
            MessageResponse cancelResponse = aiChatService.cancelAdminHandoff(roomId);
            
            // 취소 메시지 브로드캐스트
            Map<String, Object> cancelPayload = Map.of(
                "roomId", roomId,
                "messageId", cancelResponse.getMessageId(),
                "senderId", cancelResponse.getSenderId(),
                "senderType", "AI",
                "content", cancelResponse.getContent(),
                "sentAt", cancelResponse.getSentAt().toString()
            );
            
            // Add room state for cancel handoff
            ChatRoom cancelRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            Map<String, Object> cancelRoomState = createRoomStateInfo(cancelRoom, "handoff_cancelled");
            
            Map<String, Object> cancelBroadcast = Map.of(
                "type", "AI_MESSAGE",
                "payload", cancelPayload,
                "roomState", cancelRoomState
            );
            
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                cancelBroadcast
            );
            
            // 버튼 상태 업데이트 브로드캐스트
            sendButtonStateUpdate(roomId, "AI_ACTIVE");
            
            log.info("핸드오프 취소 처리 완료 - roomId: {}, userId: {}", roomId, userId);
            
        } catch (Exception e) {
            log.error("핸드오프 취소 처리 실패 - roomId: {}", message.get("roomId"), e);
            sendErrorMessage(headerAccessor, "핸드오프 취소에 실패했습니다.");
        }
    }

    /**
     * 관리자 사전 개입 (AI_ACTIVE 상태에서 직접 관리자가 개입)
     * /app/proactive-intervention -> AI_ACTIVE에서 바로 HUMAN_ACTIVE로 전환
     */
    @MessageMapping("/proactive-intervention")
    public void proactiveIntervention(@Payload Map<String, Object> message,
                                    SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            
            // Get current room and verify it's in AI_ACTIVE state
            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId)
                .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
                
            if (currentRoom.getCurrentState() != ChatRoom.ChatRoomState.AI_ACTIVE) {
                throw new IllegalStateException("AI 활성 상태가 아닌 방에서는 사전 개입할 수 없습니다");
            }
            
            log.info("관리자 사전 개입 시작 - roomId: {}, userId: {}, currentState: {}", 
                roomId, userId, currentRoom.getCurrentState());
            
            // Determine admin code based on room type
            String adminCode;
            if (roomId.startsWith("platform-")) {
                adminCode = "PLATFORM_ADMIN";
            } else {
                adminCode = chatWebSocketService.determineAdminCode(userId, ADMIN_CODE_TYPE);
            }
            
            // Use consistent handoff process like acceptHandoff for consistency
            chatRoomService.handoffAIToAdmin(roomId, adminCode);
            // Refresh the chatRoom from DB to get the updated state
            ChatRoom savedRoom = chatRoomRepository.findByRoomCode(roomId)
                .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
            log.info("🔧 Room saved after intervention - roomId: {}, state: {}, hasAssignedAdmin: {}", 
                    savedRoom.getRoomCode(), savedRoom.getCurrentState(), savedRoom.hasAssignedAdmin());
            
            // handoffAIToAdmin already handles system message and WebSocket broadcasts
            // No additional messages needed to avoid duplication
            
            log.info("관리자 사전 개입 완료 - roomId: {}, userId: {}, newState: {}", 
                roomId, userId, savedRoom.getCurrentState());
            
        } catch (Exception e) {
            log.error("관리자 사전 개입 실패 - roomId: {}", message.get("roomId"), e);
            sendErrorMessage(headerAccessor, "관리자 개입에 실패했습니다.");
        }
    }

    /**
     * 관리자 인계 수락 (WAITING_FOR_ADMIN → ADMIN_ACTIVE)
     * 사용자가 요청한 관리자 연결을 관리자가 수락
     */
    @MessageMapping("/accept-handoff")
    public void acceptHandoff(@Payload Map<String, Object> message,
                             SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            
            // Determine admin code based on room type (same logic as proactiveIntervention)
            String adminCode;
            if (roomId.startsWith("platform-")) {
                adminCode = "PLATFORM_ADMIN";
            } else {
                adminCode = chatWebSocketService.determineAdminCode(userId, ADMIN_CODE_TYPE);
            }
            
            // Get current room and verify it's in WAITING_FOR_ADMIN state
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
            
            if (chatRoom.getCurrentState() != ChatRoom.ChatRoomState.WAITING_FOR_ADMIN) {
                throw new IllegalStateException("채팅방이 관리자 대기 상태가 아닙니다: " + chatRoom.getCurrentState());
            }
            
            log.info("관리자 인계 수락 시작 - roomCode: {}, adminCode: {}, currentState: {}", 
                roomId, adminCode, chatRoom.getCurrentState());
            
            // Call AI handoff system for proper summary and transition
            chatRoomService.handoffAIToAdmin(roomId, adminCode);
            
            // Refresh the chatRoom from DB to get the updated state
            chatRoom = chatRoomRepository.findByRoomCode(roomId)
                .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
            
            // Save handoff acceptance system message to database for persistence
            ChatMessage acceptSystemMessage = ChatMessage.createSystemMessage(
                roomId, 
                "ADMIN_HANDOFF_ACCEPTED:관리자가 상담에 참여했습니다.\n더 자세하고 전문적인 도움을 드리겠습니다."
            );
            ChatMessage savedSystemMessage = chatMessageRepository.save(acceptSystemMessage);
            
            // Send handoff acceptance system message (not a regular chat message)
            Map<String, Object> systemMessagePayload = Map.of(
                "type", "ADMIN_HANDOFF_ACCEPTED",
                "roomCode", roomId,
                "adminName", chatRoom.getAdminDisplayName(),
                "timestamp", java.time.LocalDateTime.now().toString(),
                "message", "관리자가 상담에 참여했습니다.\n더 자세하고 전문적인 도움을 드리겠습니다.",
                "messageId", savedSystemMessage.getId()
            );
            
            // Create room state info for handoff acceptance
            Map<String, Object> acceptRoomState = createRoomStateInfo(chatRoom, "handoff_accepted");
            
            // Broadcast system message (not a regular chat message)
            Map<String, Object> broadcastMessage = Map.of(
                "type", "SYSTEM_MESSAGE",
                "payload", systemMessagePayload,
                "roomState", acceptRoomState
            );
            
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                broadcastMessage
            );
            
            // Update button state to ADMIN_ACTIVE
            sendButtonStateUpdate(roomId, "ADMIN_ACTIVE");
            
            log.info("관리자 인계 수락 완료 - roomCode: {}, adminCode: {}, newState: {}", 
                roomId, adminCode, chatRoom.getCurrentState());
            
        } catch (Exception e) {
            log.error("관리자 인계 수락 실패 - roomId: {}, error: {}", 
                message.get("roomId"), e.getMessage(), e);
            
            String sessionId = headerAccessor.getSessionId();
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "error", "ACCEPT_HANDOFF_FAILED",
                "message", "관리자 인계 수락에 실패했습니다: " + e.getMessage()
            );
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", error);
        }
    }

    /**
     * AI 복귀 요청 (버튼 액션)
     * /app/request-ai -> 관리자에서 AI로 전환
     */
    @MessageMapping("/request-ai")
    public void requestAI(@Payload Map<String, Object> message,
                         SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            
            // AI 서비스를 통한 AI 복귀 처리
            MessageResponse aiReturnResponse = aiChatService.requestAIReturn(roomId);
            
            // AI 복귀 메시지 브로드캐스트
            Map<String, Object> returnPayload = Map.of(
                "roomId", roomId,
                "messageId", aiReturnResponse.getMessageId(),
                "senderId", aiReturnResponse.getSenderId(),
                "senderType", "AI",
                "content", aiReturnResponse.getContent(),
                "sentAt", aiReturnResponse.getSentAt().toString()
            );
            
            // Add room state for AI return
            ChatRoom returnRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            Map<String, Object> returnRoomState = createRoomStateInfo(returnRoom, "ai_return_requested");
            
            Map<String, Object> returnBroadcast = Map.of(
                "type", "AI_RETURN",
                "payload", returnPayload,
                "roomState", returnRoomState
            );
            
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                returnBroadcast
            );
            
            // 버튼 상태 업데이트 브로드캐스트
            sendButtonStateUpdate(roomId, "AI_ACTIVE");
            
            log.info("AI 복귀 처리 완료 - roomId: {}, userId: {}", roomId, userId);
            
        } catch (Exception e) {
            log.error("AI 복귀 처리 실패 - roomId: {}", message.get("roomId"), e);
            sendErrorMessage(headerAccessor, "AI 복귀 요청에 실패했습니다.");
        }
    }
    
    /**
     * 룸 코드에서 박람회 ID 추출
     * roomCode 형식: admin-{expoId}-{userId}
     */
    private Long extractExpoIdFromRoomCode(String roomCode) {
        try {
            if (roomCode != null && roomCode.startsWith(ADMIN_ROOM_PREFIX)) {
                String[] parts = roomCode.split(ROOM_DELIMITER);
                if (parts.length >= 3) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid room code format for expoId extraction: {}", roomCode);
        }
        return null;
    }
    
    // 중복 메서드들 제거됨 - ChatUnreadService로 통합
    
    /**
     * 버튼 상태 업데이트 브로드캐스트 (상태 기반)
     */
    private void sendButtonStateUpdate(String roomId, String newState) {
        try {
            // Get current room state for accurate state information
            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            Map<String, Object> buttonRoomState = createRoomStateInfo(currentRoom, "button_state_update");
            
            Map<String, Object> statePayload = Map.of(
                "roomId", roomId,
                "state", newState,
                "buttonText", getButtonText(newState),
                "buttonAction", getButtonAction(newState)
            );
            
            Map<String, Object> stateBroadcast = Map.of(
                "type", "BUTTON_STATE_UPDATE",
                "payload", statePayload,
                "roomState", buttonRoomState
            );
            
            String channel = "/topic/chat/" + roomId;
            log.warn(" DEBUG: sendButtonStateUpdate - roomId: {}, state: {}, channel: {}", 
                roomId, newState, channel);
            log.warn(" DEBUG: BUTTON_STATE_UPDATE payload: {}", stateBroadcast);
            
            messagingTemplate.convertAndSend(channel, stateBroadcast);
            
            log.warn(" DEBUG: BUTTON_STATE_UPDATE sent successfully to {}", channel);
            
        } catch (Exception e) {
            log.warn("버튼 상태 업데이트 전송 실패 - roomId: {}, state: {}", roomId, newState, e);
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
     * 채팅방 상태 델타 정보 생성 (효율적인 state broadcasting)
     * 변경된 필드만 전송하여 네트워크 효율성 향상
     */
    private Map<String, Object> createRoomStateDelta(ChatRoom chatRoom, String transitionReason, ChatRoom.ChatRoomState previousState) {
        if (chatRoom == null) {
            return Map.of(
                "current", "AI_ACTIVE",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "unknown"
            );
        }
        
        ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
        Map<String, Object> delta = new java.util.HashMap<>();
        
        // Always include current state and timestamp
        delta.put("current", currentState.name());
        delta.put("timestamp", java.time.LocalDateTime.now().toString());
        delta.put("transitionReason", transitionReason != null ? transitionReason : "message_flow");
        
        // Only include description and buttonText if state actually changed
        if (previousState == null || !previousState.equals(currentState)) {
            delta.put("description", currentState.getDescription());
            delta.put("buttonText", currentState.getButtonText());
            delta.put("stateChanged", true);
        } else {
            delta.put("stateChanged", false);
        }
        
        // Add admin info only for admin active states (conditional data)
        if (currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE && chatRoom.hasAssignedAdmin()) {
            delta.put("adminInfo", Map.of(
                "adminCode", chatRoom.getCurrentAdminCode(),
                "displayName", chatRoom.getAdminDisplayName() != null ? chatRoom.getAdminDisplayName() : "관리자",
                "lastActivity", chatRoom.getLastAdminActivity() != null ? chatRoom.getLastAdminActivity().toString() : ""
            ));
        }
        
        // Add handoff info only for waiting state (conditional data)
        if (currentState == ChatRoom.ChatRoomState.WAITING_FOR_ADMIN && chatRoom.getHandoffRequestedAt() != null) {
            delta.put("handoffInfo", Map.of(
                "requestedAt", chatRoom.getHandoffRequestedAt().toString(),
                "aiSummaryGenerated", false
            ));
        }
        
        return delta;
    }

    /**
     * 채팅방 상태 정보 생성 (모든 WebSocket 메시지에 포함)
     * Legacy method for backward compatibility
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
        
        // Add handoff info for waiting state
        if (currentState == ChatRoom.ChatRoomState.WAITING_FOR_ADMIN && chatRoom.getHandoffRequestedAt() != null) {
            Map<String, Object> handoffInfo = Map.of(
                "requestedAt", chatRoom.getHandoffRequestedAt().toString(),
                "aiSummaryGenerated", false // Will be true after handoff completion
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
    
    /**
     * 관리자 코드 결정
     */
    private String determineAdminCode(String roomCode, Long userId, SimpMessageHeaderAccessor headerAccessor) {
        if (roomCode.startsWith("platform-")) {
            return "PLATFORM_ADMIN";
        }

        String token = (String) headerAccessor.getSessionAttributes().get("token");
        try {
            String loginType = jwtUtil.getLoginTypeFromToken(token);
            if ("ADMIN_CODE".equals(loginType)) {
                return chatWebSocketService.determineAdminCode(userId, loginType);
            } else {
                return "SUPER_ADMIN";
            }
        } catch (Exception e) {
            log.warn("JWT 토큰 파싱 실패 - userId: {}", userId);
            throw new IllegalStateException("JWT 토큰 파싱 실패");
        }
    }

    /**
     * 에러 메시지 전송
     */
    private void sendErrorMessage(SimpMessageHeaderAccessor headerAccessor, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", errorMessage
            );

            String sessionId = headerAccessor.getSessionId();
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                error
            );

        } catch (Exception e) {
            log.error("에러 메시지 전송 실패: {}", errorMessage, e);
        }
    }
}