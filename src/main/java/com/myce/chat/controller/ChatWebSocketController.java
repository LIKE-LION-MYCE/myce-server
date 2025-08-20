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
            
            // DEBUGGING: Log every message send to identify infinite loop source
            log.warn("🔥 DEBUG: sendMessage called - userId: {}, roomId: {}, content: '{}', sessionId: {}", 
                userId, roomId, content != null ? content.substring(0, Math.min(content.length(), 50)) : "null",
                headerAccessor.getSessionId());
            
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                userId, roomId, content, token
            );
            
            Map<String, Object> payload = Map.of(
                "roomId", roomId,
                "messageId", messageResponse.getMessageId(),
                "senderId", messageResponse.getSenderId(),
                "senderType", messageResponse.getSenderType(),
                "senderName", messageResponse.getSenderName() != null ? messageResponse.getSenderName() : "사용자", // Use correct sender name
                "content", messageResponse.getContent(),
                "sentAt", messageResponse.getSentAt().toString()
            );
            
            // Add room state information to all messages
            ChatRoom roomForState = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            Map<String, Object> roomState = createRoomStateInfo(roomForState, "user_message");
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "MESSAGE",
                "payload", payload,
                "roomState", roomState
            );
                    
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                broadcastMessage
            );
            
            // 🆕 상태 기반 발송자 본인 메시지 즉시 읽음 처리
            // 데이터베이스에서 최신 채팅방 상태를 다시 조회하여 정확한 상태 확인
            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            if (currentRoom != null) {
                ChatRoom.ChatRoomState currentState = currentRoom.getCurrentState();
                
                // 플랫폼 채팅방에서 AI_ACTIVE 또는 ADMIN_ACTIVE 상태일 때 자동 읽음 처리
                boolean shouldAutoRead = roomId.startsWith("platform-") && 
                    (currentState == ChatRoom.ChatRoomState.AI_ACTIVE || 
                     currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE);
                
                if (shouldAutoRead) {
                    try {
                        // 발송자 본인 메시지 즉시 읽음 처리
                        Member sender = memberRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
                        String senderRole = sender.getRole().name();
                        
                        chatRoomService.markAsRead(roomId, messageResponse.getMessageId(), userId, senderRole);
                        log.debug("✅ 플랫폼 상담 중 발송자 본인 메시지 읽음 처리 완료 - userId: {}, roomId: {}, state: {}, messageId: {}", 
                                  userId, roomId, currentState, messageResponse.getMessageId());
                        
                        // 🆕 플랫폼 관리자가 메시지를 보낸 경우 → 해당 유저의 미읽음 메시지도 자동 읽음 처리
                        if ("PLATFORM_ADMIN".equals(senderRole) && 
                            currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE) {
                            
                            String[] roomParts = roomId.split("-");
                            Long platformUserId = Long.parseLong(roomParts[1]);
                            
                            // 유저의 미읽은 메시지들을 모두 읽음 처리
                            chatRoomService.markAsRead(roomId, messageResponse.getMessageId(), platformUserId, "USER");
                            log.debug("✅ 플랫폼 관리자 답장으로 인한 유저 미읽음 자동 처리 완료 - platformUserId: {}, roomId: {}, messageId: {}", 
                                      platformUserId, roomId, messageResponse.getMessageId());
                        }
                        
                        // 🔔 읽음 상태 변경을 WebSocket으로 브로드캐스트
                        Map<String, Object> readStatusUpdate = Map.of(
                            "type", "read_status_update",
                            "messageId", messageResponse.getMessageId(),
                            "readBy", userId,
                            "readerType", "USER",
                            "timestamp", System.currentTimeMillis()
                        );
                        
                        messagingTemplate.convertAndSend(
                            "/topic/chat/" + roomId,
                            readStatusUpdate
                        );
                        
                        log.debug("🔔 읽음 상태 WebSocket 브로드캐스트 완료 - roomId: {}, messageId: {}", 
                                  roomId, messageResponse.getMessageId());
                    } catch (Exception e) {
                        log.warn("⚠️ 플랫폼 상담 중 발송자 본인 메시지 읽음 처리 실패 - userId: {}, roomId: {}, state: {}, error: {}", 
                                 userId, roomId, currentState, e.getMessage());
                    }
                }
            }
            
            // AI 응답 처리 (상태 기반)
            boolean isAIEnabled = aiChatService.isAIEnabled(roomId);
            
            if (currentRoom != null && isAIEnabled) {
                ChatRoom.ChatRoomState currentState = currentRoom.getCurrentState();
                
                log.warn("🤖 DEBUG: AI 응답 조건 확인 - roomId: {}, AI활성화: {}, 현재상태: {}, 트리거된유저: {}", 
                    roomId, isAIEnabled, currentState, userId);
                
                // AI는 AI_ACTIVE 또는 WAITING_FOR_ADMIN 상태에서만 응답
                // (ADMIN_ACTIVE 상태에서는 AI 응답 안함)
                boolean shouldAIRespond = (currentState == ChatRoom.ChatRoomState.AI_ACTIVE || 
                                         currentState == ChatRoom.ChatRoomState.WAITING_FOR_ADMIN);
                
                if (shouldAIRespond) {
                try {
                    MessageResponse aiResponse = aiChatService.sendAIMessage(roomId, content);
                    
                    Map<String, Object> aiPayload = Map.of(
                        "roomId", roomId,
                        "messageId", aiResponse.getMessageId(),
                        "senderId", aiResponse.getSenderId(),
                        "senderType", "AI",
                        "content", aiResponse.getContent(),
                        "sentAt", aiResponse.getSentAt().toString()
                    );
                    
                    Map<String, Object> aiRoomState = createRoomStateInfo(currentRoom, "ai_response");
                    
                    Map<String, Object> aiBroadcastMessage = Map.of(
                        "type", "AI_MESSAGE",
                        "payload", aiPayload,
                        "roomState", aiRoomState
                    );
                    
                    log.warn("🤖 DEBUG: AI 메시지 브로드캐스트 시작 - topic: /topic/chat/{}, payload: {}", 
                        roomId, aiPayload);
                    
                    messagingTemplate.convertAndSend(
                        "/topic/chat/" + roomId,
                        aiBroadcastMessage
                    );
                    
                    log.warn("🤖 DEBUG: AI 응답 전송 완료 - roomId: {}", roomId);
                    
                } catch (Exception aiError) {
                    log.error("AI 응답 처리 실패 - roomId: {}", roomId, aiError);
                }
                } else {
                    log.debug("AI 응답 건너뜀 - roomId: {}, 상태: {}, 이유: AI가 응답할 수 없는 상태", 
                        roomId, currentState);
                }
            } else {
                log.debug("AI 응답 건너뜀 - roomId: {}, 이유: AI비활성화={}, 방없음={}", 
                    roomId, !isAIEnabled, currentRoom == null);
            }
            
            // 사용자 메시지 전송 후 박람회 관리자들에게 unread count 업데이트 알림
            try {
                Long expoId = extractExpoIdFromRoomCode(roomId);
                if (expoId != null) {
                    // 채팅방의 현재 unread count 조회
                    ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId)
                        .orElse(null);
                    
                    if (chatRoom != null) {
                        // 관리자가 마지막으로 읽은 메시지 이후의 USER 메시지만 계산
                        Long unreadCount = chatUnreadService.getUnreadCountForViewer(roomId, 0L, "EXPO_ADMIN");
                        
                        Map<String, Object> unreadUpdatePayload = Map.of(
                            "roomCode", roomId,
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
                    }
                }
            } catch (Exception unreadUpdateError) {
                log.warn("Unread count 업데이트 전송 실패 - roomCode: {}, error: {}", 
                    roomId, unreadUpdateError.getMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ 메시지 전송 실패 - roomId: {}, userId: {}, error: {}", 
                     message.get("roomId"), userId, e.getMessage(), e);
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "Send message failed: " + e.getMessage(),
                "detail", e.getClass().getSimpleName()
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
     * 관리자 채팅 메시지 전송
     * /app/admin/chat.send -> 관리자 권한 검증 -> 담당자 배정 -> 메시지 저장 및 브로드캐스트
     */
    @MessageMapping("/admin/chat.send")
    public void sendAdminMessage(@Payload Map<String, Object> message,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomCode = (String) message.get("roomCode");
            String content = (String) message.get("message");
            // Platform rooms have expoId = null, expo rooms have actual expoId
            Long expoId = message.get("expoId") != null ? ((Number) message.get("expoId")).longValue() : null;
            
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
            
            // Determine admin code based on room type and user
            String adminCode;
            if (roomCode.startsWith("platform-")) {
                // Platform admin handling platform rooms
                adminCode = "PLATFORM_ADMIN";
            } else {
                // Regular expo admin handling expo rooms - extract from JWT token directly
                String token = (String) headerAccessor.getSessionAttributes().get("token");
                
                try {
                    String loginType = jwtUtil.getLoginTypeFromToken(token);
                    
                    if ("ADMIN_CODE".equals(loginType)) {
                        // Use the existing service method to determine admin code
                        adminCode = chatWebSocketService.determineAdminCode(userId, loginType);
                    } else {
                        adminCode = "SUPER_ADMIN";
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("JWT 토큰 파싱 실패");
                }
            }
            
            // Assign admin if needed for expo rooms FIRST
            if (!roomCode.startsWith("platform-")) {
                chatWebSocketService.assignAdminIfNeeded(chatRoom, adminCode);
            }
            
            // THEN check admin collision protection after assignment
            if (chatRoom.hasAssignedAdmin() && !chatRoom.hasAdminPermission(adminCode)) {
                String errorMsg = String.format("상담 권한이 없습니다. 현재 담당자: %s", 
                                                chatRoom.getAdminDisplayName());
                
                // Send error message back to the unauthorized admin
                Map<String, Object> errorPayload = Map.of(
                    "error", "PERMISSION_DENIED",
                    "message", errorMsg,
                    "currentAdmin", chatRoom.getAdminDisplayName()
                );
                messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/errors",
                    errorPayload
                );
                return;
            }
            

            // State-driven admin assignment logic (only for platform rooms)
            if (roomCode.startsWith("platform-")) {
                ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
                log.debug("Platform admin message handling - roomCode: {}, currentState: {}", roomCode, currentState);
                
                switch (currentState) {
                    case WAITING_FOR_ADMIN -> {
                        // Call AI handoff system for proper summary and transition
                        try {
                            chatRoomService.handoffAIToAdmin(roomCode, adminCode);
                            // Refresh the chatRoom from DB to get the updated state
                            chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                                .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
                            log.info("AI handoff completed - roomCode: {}, adminCode: {}, newState: {}", 
                                roomCode, adminCode, chatRoom.getCurrentState());
                        } catch (Exception handoffError) {
                            log.error("AI handoff failed - roomCode: {}, adminCode: {}", roomCode, adminCode, handoffError);
                        }
                    }
                    case AI_ACTIVE -> {
                        // Block direct messaging during AI chat - admins must use intervention button
                        log.warn("❌ Direct admin message blocked during AI_ACTIVE state - roomCode: {}, adminCode: {}", 
                            roomCode, adminCode);
                        
                        // Send error message back to admin
                        Map<String, Object> errorPayload = Map.of(
                            "error", "INTERVENTION_REQUIRED",
                            "message", "AI 상담 중에는 직접 메시지를 보낼 수 없습니다. '개입하기' 버튼을 사용해주세요.",
                            "suggestedAction", "USE_INTERVENTION_BUTTON"
                        );
                        messagingTemplate.convertAndSendToUser(
                            userId.toString(),
                            "/queue/errors",
                            errorPayload
                        );
                        return; // Block the message entirely
                    }
                    case ADMIN_ACTIVE -> {
                        // Admin already active - just update admin activity
                        chatRoom.updateAdminActivity();
                        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
                        // Redis 캐시 동기화 (Super/AdminCode 구분 로직 보존)
                        chatCacheService.cacheChatRoom(roomCode, savedRoom);
                        log.debug("Admin activity updated and cached - roomCode: {}, state: {}", roomCode, currentState);
                    }
                }
            }
            
            // 담당자 배정 브로드캐스트
            if (chatRoom.hasAssignedAdmin()) {
                Map<String, Object> assignmentPayload = Map.of(
                    "roomCode", roomCode,
                    "currentAdminCode", chatRoom.getCurrentAdminCode(),
                    "adminDisplayName", chatRoom.getAdminDisplayName()
                );
                
                Map<String, Object> assignmentMessage = Map.of(
                    "type", "admin_assignment_update",
                    "payload", assignmentPayload
                );
                
                messagingTemplate.convertAndSend(
                    "/topic/chat/" + roomCode,
                    assignmentMessage
                );
                
                messagingTemplate.convertAndSend(
                    "/topic/expo/" + expoId + "/admin-updates",
                    assignmentMessage
                );
            }
            
            // Use sendMessage service for complete save including Redis cache and ChatRoom update
            String token = (String) headerAccessor.getSessionAttributes().get("token");
            MessageResponse messageResponse = chatWebSocketService.sendMessage(userId, roomCode, content, token);
            
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
            
            // Add room state information to admin messages
            Map<String, Object> adminRoomState = createRoomStateInfo(chatRoom, "admin_message");
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "ADMIN_MESSAGE",
                "payload", payload,
                "roomState", adminRoomState
            );
                    
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomCode,
                broadcastMessage
            );
            
        } catch (CustomException e) {
            CustomErrorCode errorCode = e.getErrorCode();
            String errorMessage = errorCode == CustomErrorCode.CHAT_ROOM_ACCESS_DENIED 
                ? "이미 다른 관리자가 담당하고 있는 상담입니다." 
                : errorCode.getMessage();
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "errorCode", errorCode.getErrorCode(),
                "message", errorMessage,
                "payload", Map.of(
                    "code", errorCode.getErrorCode(),
                    "message", errorMessage
                )
            );
                    
            String sessionId = headerAccessor.getSessionId();
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            
            try {
                // Send error to the channel that frontend subscribes to
                String generalErrorChannel = "/topic/user/errors";
                messagingTemplate.convertAndSend(generalErrorChannel, error);
                
                // Also send to user-specific channels for better coverage
                String userErrorChannel = USER_ERROR_TOPIC_PREFIX + sessionId + ERROR_CHANNEL_SUFFIX;
                messagingTemplate.convertAndSend(userErrorChannel, error);
                
                // Also send to user-specific queue
                messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/errors", 
                    error
                );
                
                // Also send to user ID based channel if available
                if (userId != null) {
                    messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/errors",
                        error
                    );
                }
                
                        
            } catch (Exception sendException) {
                log.error("에러 메시지 전송 실패", sendException);
            }
            
        } catch (Exception e) {
            log.error("관리자 메시지 전송 실패 - roomCode: {}, error: {}", message.get("roomCode"), e.getMessage(), e);
            log.error("🚨 Exception stack trace:", e);
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "메시지 전송에 실패했습니다: " + e.getMessage()
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
            
            log.warn("🔍 DEBUG HANDOFF REQUEST - roomId: {}, userId: {}, sessionId: {}", 
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
            log.warn("🔍 DEBUG: Sending AI_HANDOFF_REQUEST to channel: {}", topicChannel);
            log.warn("🔍 DEBUG: Message payload: {}", handoffBroadcast);
            
            messagingTemplate.convertAndSend(topicChannel, handoffBroadcast);
            
            log.warn("🔍 DEBUG: AI_HANDOFF_REQUEST sent successfully");
            
            // 버튼 상태 업데이트 브로드캐스트
            log.warn("🔍 DEBUG: Sending BUTTON_STATE_UPDATE to channel: {}", topicChannel);
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
            log.warn("🔍 DEBUG: sendButtonStateUpdate - roomId: {}, state: {}, channel: {}", 
                roomId, newState, channel);
            log.warn("🔍 DEBUG: BUTTON_STATE_UPDATE payload: {}", stateBroadcast);
            
            messagingTemplate.convertAndSend(channel, stateBroadcast);
            
            log.warn("🔍 DEBUG: BUTTON_STATE_UPDATE sent successfully to {}", channel);
            
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