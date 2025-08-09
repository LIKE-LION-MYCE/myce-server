package com.myce.chat.controller;

import com.myce.chat.dto.MessageResponse;
import com.myce.chat.service.ChatWebSocketService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 인증 처리
     * /app/auth -> JWT 토큰 검증 -> 세션에 사용자 ID 저장
     */
    @MessageMapping("/auth")
    public void authenticate(@Payload Map<String, Object> message,
            SimpMessageHeaderAccessor headerAccessor) {
        log.info("WebSocket 인증 요청 수신");

        try {
            String token = (String) message.get("token");

            Long userId = chatWebSocketService.authenticateUser(token);

            // 세션에 사용자 정보 저장
            headerAccessor.getSessionAttributes().put("userId", userId);

            // 인증 성공 응답
            Map<String, Object> authAck = Map.of(
                    "type", "AUTH_ACK",
                    "payload", "Authentication successful",
                    "userId", userId
            );

            // 토픽으로 인증 성공 응답 전송 (세션별 개별 응답)
            String sessionId = headerAccessor.getSessionId();
            Map<String, Object> authResponse = Map.of(
                    "type", "AUTH_ACK",
                    "payload", "Authentication successful",
                    "userId", userId,
                    "sessionId", sessionId
            );

            messagingTemplate.convertAndSend("/topic/auth-test", authResponse);
            log.info("WebSocket 인증 성공 - userId: {}", userId);

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
        log.info("채팅방 입장 요청 수신");

        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }

            String roomId = (String) message.get("roomId");

            // 채팅방 입장 권한 검증 및 처리
            chatWebSocketService.joinRoom(userId, roomId);

            // 세션에 현재 방 정보 저장
            headerAccessor.getSessionAttributes().put("currentRoomId", roomId);

            log.info("채팅방 입장 성공 - userId: {}, roomId: {}", userId, roomId);

        } catch (Exception e) {
            log.error("채팅방 입장 실패", e);

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
        log.info("메시지 전송 요청 수신");

        try {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            if (userId == null) {
                throw new IllegalStateException("인증되지 않은 사용자");
            }

            String roomId = (String) message.get("roomId");
            String content = (String) message.get("message");

            // 메시지 저장 및 브로드캐스트
            MessageResponse messageResponse = chatWebSocketService.sendMessage(
                    userId,
                    roomId,
                    content
            );

            // 채팅방 구독자들에게 메시지 브로드캐스트 (WebSocket 규격 문서에 맞춤)
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

            log.info("메시지 전송 완료 - userId: {}, roomId: {}", userId, roomId);

        } catch (Exception e) {
            log.error("메시지 전송 실패", e);

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
}