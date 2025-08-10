package com.myce.chat.controller;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.*;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatWebSocketService;
// import com.myce.chat.service.AdminWebSocketService; // TODO: 추후 구현
import com.myce.chat.type.WebSocketMessageType;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.CustomErrorCode;
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

    private final ChatWebSocketService chatWebSocketService;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 인증 처리
     * /app/auth -> JWT 토큰 검증 -> 세션에 사용자 ID 저장
     */
    @MessageMapping("/auth")
    public void authenticate(@Payload Map<String, Object> message, 
                           SimpMessageHeaderAccessor headerAccessor) {
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
            
            messagingTemplate.convertAndSend("/topic/auth-test", authResponse);
            
        } catch (Exception e) {
            log.error("WebSocket 인증 실패", e);
            
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "payload", "Authentication failed: " + e.getMessage()
            );
            
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
            
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                userId, roomId, content
            );
            
            Map<String, Object> payload = Map.of(
                "roomId", roomId,
                "messageId", messageResponse.getMessageId(),
                "senderId", messageResponse.getSenderId(),
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
            Long expoId = ((Number) message.get("expoId")).longValue();
            
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다"));
            
            String adminCode = chatWebSocketService.determineAdminCode(userId, "ADMIN_CODE");
            
            chatWebSocketService.assignAdminIfNeeded(chatRoom, adminCode);
            chatRoomRepository.save(chatRoom);
            
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
            
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                userId, roomCode, content
            );
            
            Map<String, Object> payload = Map.of(
                "roomCode", roomCode,
                "messageId", messageResponse.getMessageId(),
                "senderId", messageResponse.getSenderId(),
                "senderType", "ADMIN",
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
                String userErrorChannel = "/topic/user/" + sessionId + "/errors";
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
}