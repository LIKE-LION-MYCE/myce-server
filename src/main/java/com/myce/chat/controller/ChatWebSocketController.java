package com.myce.chat.controller;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.*;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatWebSocketService;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.WebSocketMessageType;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.CustomErrorCode;
import com.myce.ai.service.AIChatService;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final AIChatService aiChatService;
    private final ChatRoomService chatRoomService;

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
        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }
            
            String roomId = (String) message.get("roomId");
            String content = (String) message.get("message");
            
            // DEBUGGING: Log every message send to identify infinite loop source
            log.warn("🔥 DEBUG: sendMessage called - userId: {}, roomId: {}, content: '{}', sessionId: {}", 
                userId, roomId, content != null ? content.substring(0, Math.min(content.length(), 50)) : "null",
                headerAccessor.getSessionId());
            
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                userId, roomId, content
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
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "MESSAGE",
                "payload", payload
            );
                    
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                broadcastMessage
            );
            
            // AI 응답 처리 (플랫폼 방에서만, 그리고 관리자가 배정되지 않은 경우에만)
            // 데이터베이스에서 최신 채팅방 상태를 다시 조회하여 정확한 상태 확인
            ChatRoom currentRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
            boolean isAIEnabled = aiChatService.isAIEnabled(roomId);
            boolean hasAdmin = currentRoom != null && currentRoom.hasAssignedAdmin();
            boolean isWaitingForAdmin = currentRoom != null && currentRoom.isWaitingForAdmin();
            
            log.warn("🤖 DEBUG: AI 응답 조건 확인 - roomId: {}, AI활성화: {}, 관리자배정: {}, 대기중: {}, 현재관리자: {}, 트리거된유저: {}", 
                roomId, isAIEnabled, hasAdmin, isWaitingForAdmin, 
                currentRoom != null ? currentRoom.getCurrentAdminCode() : "없음", userId);
            
            // AI는 다음 경우에만 응답:
            // 1. AI가 활성화된 방이고
            // 2. 관리자가 배정되지 않았고
            // 3. 관리자 대기 중이어도 AI는 계속 응답 (완전 인계 전까지)
            if (isAIEnabled && !hasAdmin) {
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
                    
                    Map<String, Object> aiBroadcastMessage = Map.of(
                        "type", "AI_MESSAGE",
                        "payload", aiPayload
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
                log.debug("AI 응답 건너뜀 - roomId: {}, 이유: AI비활성화={}, 관리자배정됨={}", 
                    roomId, !isAIEnabled, hasAdmin);
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
                        Integer unreadCount = calculateUnreadCountForAdmin(chatRoom);
                        
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
            log.error("메시지 전송 실패 - roomId: {}", message.get("roomId"));
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "Send message failed: " + e.getMessage()
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
                // Regular expo admin handling expo rooms  
                adminCode = chatWebSocketService.determineAdminCode(userId, ADMIN_CODE_TYPE);
            }
            
            // If room is waiting for admin, call AI handoff system
            if (chatRoom.isWaitingForAdmin()) {
                // Call AI handoff system for proper summary and transition
                try {
                    chatRoomService.handoffAIToAdmin(roomCode, adminCode);
                    log.info("AI handoff completed for waiting room - roomCode: {}, adminCode: {}, hasAdmin: {}", 
                        roomCode, adminCode, chatRoom.hasAssignedAdmin());
                } catch (Exception handoffError) {
                    log.error("AI handoff failed - roomCode: {}, adminCode: {}", roomCode, adminCode, handoffError);
                }
            } else {
                // Regular admin assignment
                log.debug("Regular admin assignment - roomCode: {}, adminCode: {}, currentAdmin: {}", 
                    roomCode, adminCode, chatRoom.getCurrentAdminCode());
                chatWebSocketService.assignAdminIfNeeded(chatRoom, adminCode);
                log.debug("After assignment - roomCode: {}, hasAdmin: {}, currentAdmin: {}", 
                    roomCode, chatRoom.hasAssignedAdmin(), chatRoom.getCurrentAdminCode());
            }
            
            // Save chatRoom changes immediately to ensure they are persisted before any AI logic runs
            ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
            log.debug("ChatRoom saved - roomCode: {}, hasAdmin: {}, currentAdmin: {}", 
                roomCode, savedChatRoom.hasAssignedAdmin(), savedChatRoom.getCurrentAdminCode());
            
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
            
            // 관리자 메시지 직접 생성 및 저장
            ChatMessage adminMessage = ChatMessage.createAdminMessage(
                roomCode, content, userId, adminCode, ADMIN_CODE_TYPE, 
                chatRoom.getAdminDisplayName()
            );
            ChatMessage savedMessage = chatMessageRepository.save(adminMessage);
            
            // ChatMessageMapper를 사용하여 일관성 있는 변환
            MessageResponse messageResponse = ChatMessageMapper.toDto(
                savedMessage, 1, adminCode, chatRoom.getAdminDisplayName()
            );
            
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
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "ADMIN_MESSAGE",
                "payload", payload
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
            try {
                String userErrorChannel = USER_ERROR_TOPIC_PREFIX + sessionId + ERROR_CHANNEL_SUFFIX;
                messagingTemplate.convertAndSend(userErrorChannel, error);
            } catch (Exception sendException) {
                log.error("에러 메시지 전송 실패", sendException);
            }
            
        } catch (Exception e) {
            log.error("관리자 메시지 전송 실패 - roomCode: {}", message.get("roomCode"));
            
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
            
            Map<String, Object> handoffBroadcast = Map.of(
                "type", "AI_HANDOFF_REQUEST",
                "payload", handoffPayload
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
            
            Map<String, Object> cancelBroadcast = Map.of(
                "type", "AI_MESSAGE",
                "payload", cancelPayload
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
            
            Map<String, Object> returnBroadcast = Map.of(
                "type", "AI_RETURN",
                "payload", returnPayload
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
    
    /**
     * 관리자 입장에서 안읽은 메시지 개수 계산
     * (관리자가 마지막으로 읽은 메시지 이후의 USER 메시지만 계산)
     */
    private Integer calculateUnreadCountForAdmin(ChatRoom chatRoom) {
        try {
            String roomCode = chatRoom.getRoomCode();
            String readStatusJson = chatRoom.getReadStatusJson();
            
            // readStatusJson에서 ADMIN의 마지막 읽은 메시지 ID 추출
            String lastReadMessageId = extractLastReadMessageId(readStatusJson, "ADMIN");
            
            if (lastReadMessageId == null || lastReadMessageId.isEmpty()) {
                // 관리자가 아직 아무것도 읽지 않았다면 전체 USER 메시지 개수
                Long count = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "USER");
                return count.intValue();
            } else {
                // 마지막 읽은 메시지 ID 이후의 USER 메시지 개수
                Long count = chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                    roomCode, "USER", lastReadMessageId);
                return count.intValue();
            }
        } catch (Exception e) {
            log.warn("안읽은 메시지 개수 계산 실패 - roomCode: {}", chatRoom.getRoomCode());
            return 0;
        }
    }
    
    /**
     * readStatusJson에서 특정 타입의 마지막 읽은 메시지 ID 추출
     */
    private String extractLastReadMessageId(String readStatusJson, String userType) {
        try {
            if (readStatusJson == null || readStatusJson.isEmpty() || readStatusJson.equals("{}")) {
                return null;
            }
            
            // 간단한 JSON 파싱 (Jackson 라이브러리 사용하지 않고)
            String searchKey = "\"" + userType + "\":\"";
            int startIndex = readStatusJson.indexOf(searchKey);
            if (startIndex == -1) {
                return null;
            }
            
            startIndex += searchKey.length();
            int endIndex = readStatusJson.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return null;
            }
            
            return readStatusJson.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.warn("readStatusJson 파싱 실패: {}", readStatusJson, e);
            return null;
        }
    }
    
    /**
     * 버튼 상태 업데이트 브로드캐스트
     */
    private void sendButtonStateUpdate(String roomId, String newState) {
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
            case "HUMAN_ACTIVE" -> "Request AI";
            case "HUMAN_INACTIVE" -> "Continue with AI";
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
            case "HUMAN_ACTIVE" -> "request_ai";
            case "HUMAN_INACTIVE" -> "request_ai";
            default -> "request_handoff";
        };
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