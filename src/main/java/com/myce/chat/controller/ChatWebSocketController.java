package com.myce.chat.controller;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.*;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatWebSocketService;
import com.myce.chat.service.mapper.ChatMessageMapper;
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
    private final ChatMessageRepository chatMessageRepository;
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
            
            // 관리자 메시지 직접 생성 및 저장
            ChatMessage adminMessage = ChatMessage.createAdminMessage(
                roomCode, content, userId, adminCode, "ADMIN_CODE", 
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
}