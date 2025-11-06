package com.myce.ai.service.impl;

import com.myce.ai.service.AIChatContextService;
import com.myce.ai.context.PublicContext;
import com.myce.ai.context.UserContext;
import com.myce.ai.facade.ChatDataFacade;
import com.myce.ai.service.AIChatService;
import com.myce.ai.service.AIChatPromptService;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.MessageSenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI 채팅 서비스 구현체
 * 
 * AWS Bedrock Nova Lite 기반 플랫폼 상담 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private static final String PLATFORM_ROOM_PREFIX = "platform-";
    // AI 식별 상수
    private static final String AI_SENDER_NAME = "찍찍봇 (AI 챗봇)";
    private static final Long AI_SENDER_ID = -1L;
    // 시스템 식별 상수
    private static final String SYSTEM_SENDER_NAME = "시스템";
    private static final Long SYSTEM_SENDER_ID = -99L;

    // 의존성 주입
    private final ChatClient chatClient;
    private final ChatDataFacade chatDataFacade;
    private final SimpMessagingTemplate messagingTemplate;
    private final AIChatPromptService aiChatPromptService;
    private final AIChatContextService aiChatContextService;

    @Override
    public String generateAIResponse(String userMessage, String roomCode) {
        try {
            // 1. 채팅방 상태 확인
            boolean isWaitingForAdmin = chatDataFacade.checkRoomIsWaitingAdmin(roomCode);
            // 2. 대화 이력 조회
            List<ChatMessage> recentMessages = chatDataFacade.getRecentMessages(roomCode);
            // 3. 컨텍스트 수집
            UserContext userContext = aiChatContextService.buildUserContext(roomCode);
            PublicContext publicContext = aiChatContextService.buildPublicContext();
            String conversationHistory = buildConversationHistory(recentMessages);
            // 4. 사람 상담 필요 여부 감지
            boolean shouldSuggestHuman = detectNeedForHumanAssistance(userMessage, recentMessages);
            // 5. AI 프롬프트 구성 (대기 상태 고려)
            String systemPrompt = aiChatPromptService.createSystemPromptWithContext(
                    userContext, publicContext, isWaitingForAdmin, shouldSuggestHuman);
            String aiPrompt = aiChatPromptService.createAIPromptWithHistoryAndUserMessage(
                    systemPrompt, conversationHistory, userMessage);
            String aiResponse = chatClient.prompt(aiPrompt).call().content();
            
            log.info("AI 응답 생성 완료 (컨텍스트 포함) - roomCode: {}, userId: {}, 대기상태: {}, 사람상담제안: {}", 
                roomCode, userContext.userId(), isWaitingForAdmin, shouldSuggestHuman);
            return aiResponse;

        } catch (Exception e) {
            log.error("AI 응답 생성 실패 - roomCode: {}", roomCode, e);
            return "찍찍! 죄송합니다. 일시적인 오류가 발생했습니다.";
        }
    }

    @Override
    @Transactional
    public MessageResponse sendAIMessage(String roomCode, String userMessage) {
        try {
            // AI 응답 생성
            String aiResponse = generateAIResponse(userMessage, roomCode);
            
            // 메시지 생성 및 저장
            ChatMessage savedMessage = chatDataFacade.saveChatMessage(roomCode, AI_SENDER_ID,
                MessageSenderType.AI.name(), AI_SENDER_NAME, aiResponse);
            
            // AI가 사용자 메시지를 "읽음" 처리 - 읽음 상태 업데이트 브로드캐스트
            sendAIReadStatusUpdate(roomCode);
            
            log.info("AI 메시지 전송 완료 (읽음 처리 포함) - roomCode: {}, messageId: {}", roomCode, savedMessage.getId());
            
            return ChatMessageMapper.toSendResponse(savedMessage, roomCode);
            
        } catch (Exception e) {
            log.error("AI 메시지 전송 실패 - roomCode: {}, userMessage: {}", roomCode, userMessage, e);
            throw new RuntimeException("AI 메시지 전송에 실패했습니다.", e);
        }
    }

    @Override
    public boolean isAIEnabled(String roomCode) {
        return roomCode != null && roomCode.startsWith(PLATFORM_ROOM_PREFIX);
    }

    @Override
    @Transactional
    public void handoffToAdmin(String roomCode, String adminCode) {
        try {
            Optional<ChatRoom> chatRoomOpt = chatDataFacade.getChatRoom(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                log.info("🔄 Starting handoff transaction - roomCode: {}, adminCode: {}, currentState: waiting={}, hasAdmin={}", 
                    roomCode, adminCode, chatRoom.isWaitingForAdmin(), chatRoom.hasAssignedAdmin());
                
                // STEP 1: IMMEDIATE STATE TRANSITION - Block AI responses first
                chatRoom.assignAdmin(adminCode);
                chatRoom.stopWaitingForAdmin();
                
                // 명시적으로 ADMIN_ACTIVE 상태로 전환 (확실하게!)
                chatRoom.transitionToState(ChatRoom.ChatRoomState.ADMIN_ACTIVE);
                
                ChatRoom savedRoom = chatDataFacade.saveChatRoom(chatRoom);
                
                // Redis 캐시 무효화 (중요!)
                chatDataFacade.cacheChatRoom(roomCode, null); // null로 설정하여 캐시 무효화
                log.info("관리자 연결 시 Redis 캐시 무효화 완료 - roomCode: {}", roomCode);
                
                log.info("Admin assigned and AI blocked - roomCode: {}, adminCode: {}, hasAdmin: {}, finalState: {}",
                    roomCode, adminCode, savedRoom.hasAssignedAdmin(), savedRoom.getCurrentState());
                
                // STEP 2: GENERATE AI SUMMARY (for system message only)
                String conversationSummary = generateConversationSummary(roomCode);
                
                // STEP 2.5: SEND HANDOFF-TO-OPERATOR SYSTEM MESSAGE (persistent) - 타입 구분
                
                ChatMessage savedSystemMessage = chatDataFacade.saveChatMessage(roomCode,
                        SYSTEM_SENDER_ID, MessageSenderType.SYSTEM.name(), SYSTEM_SENDER_NAME, conversationSummary);
                
                // Broadcast system message (not regular chat message)
                Map<String, Object> systemMessagePayload = Map.of(
                    "type", "HANDOFF_TO_OPERATOR",
                    "roomCode", roomCode,
                    "adminName", savedRoom.getAdminDisplayName(),
                    "timestamp", LocalDateTime.now().toString(),
                    "aiSummary", conversationSummary,
                    "messageId", savedSystemMessage.getId()
                );
                
                Map<String, Object> roomState = createRoomStateInfo(savedRoom, "handoff_to_operator");
                Map<String, Object> broadcastMessage = Map.of(
                    "type", "SYSTEM_MESSAGE",
                    "payload", systemMessagePayload,
                    "roomState", roomState
                );
                
                messagingTemplate.convertAndSend("/topic/chat/" + roomCode, broadcastMessage);
                
                // STEP 3: UPDATE BUTTON STATE TO ADMIN_ACTIVE
                broadcastButtonStateUpdate(roomCode, "ADMIN_ACTIVE");
                
                log.info("Complete handoff workflow finished - roomCode: {}, adminCode: {}, finalState: hasAdmin={}, currentState: {}",
                    roomCode, adminCode, savedRoom.hasAssignedAdmin(), savedRoom.getCurrentState());
            }
        } catch (Exception e) {
            log.error("AI handoff failed - roomCode: {}, adminCode: {}", roomCode, adminCode, e);
            throw new RuntimeException("관리자 handoff에 실패했습니다.", e);
        }
    }

    @Override
    public String generateConversationSummary(String roomCode) {
        try {
            // 전체 대화 이력 조회 (최근 50개)
            List<ChatMessage> allMessages = chatDataFacade.getRecentMessages(roomCode);
            if (allMessages.isEmpty()) {
                return "찍찍! 대화 내용이 없어 요약할 내용이 없습니다.";
            }
            // 시간순으로 정렬 (오래된 것부터)
            List<ChatMessage> sortedMessages = allMessages.stream()
                .sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
                .toList();
            // 사용자 컨텍스트 구성
            UserContext userContext = aiChatContextService.buildUserContext(roomCode);
            // 대화 이력을 문자열로 변환
            StringBuilder conversationLog = new StringBuilder();
            sortedMessages.forEach(msg -> {
                String senderLabel = MessageSenderType.AI.name().equals(msg.getSenderType()) 
                    ? "AI 상담사" : "고객";
                conversationLog.append(String.format("[%s] %s: %s\n", 
                    msg.getSentAt().toString(), senderLabel, msg.getContent()));
            });
            // AI 요약 프롬프트 구성 (사용자와 관리자 모두 볼 수 있도록 전문적이고 친화적으로)
            String summaryPrompt = aiChatPromptService.createSummaryPromptWithContextAndLog(
                    userContext, conversationLog);
            String summary = chatClient.prompt(summaryPrompt).call().content();
            
            log.info("대화 요약 생성 완료 - roomCode: {}, 메시지 수: {}", roomCode, sortedMessages.size());
            return summary;
            
        } catch (Exception e) {
            log.error("대화 요약 생성 실패 - roomCode: {}", roomCode, e);
            return "찍찍! 죄송합니다. 대화 요약 생성 중 오류가 발생했습니다.";
        }
    }

    @Override
    @Transactional
    public MessageResponse requestAdminHandoff(String roomCode) {
        try {
            // 1. 채팅방 조회
            Optional<ChatRoom> chatRoomOpt = chatDataFacade.getChatRoom(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();

                
                ChatMessage savedMessage = chatDataFacade.saveChatMessage(
                    roomCode,AI_SENDER_ID, MessageSenderType.AI.name(), AI_SENDER_NAME,
                        "찍찍! 상담원을 찾고 있어요. 잠시만 기다려주세요! 그동안 다른 궁금한 점이 있으시면 언제든 말씀해주세요.");
                
                // 4. 이제 채팅방 상태를 대기 상태로 변경 (AI 메시지 후에!)
                chatRoom.startWaitingForAdmin();
                chatDataFacade.saveChatRoom(chatRoom);
                
                // 🗑️ Redis 캐시 무효화 (중요!)
                chatDataFacade.cacheChatRoom(roomCode, null); // null로 설정하여 캐시 무효화
                log.info("🗑️ 상담원 연결 요청 시 Redis 캐시 무효화 완료 - roomCode: {}", roomCode);
                
                log.info("✅ 관리자 연결 요청 시작 - roomCode: {}, finalState: {} (AI 메시지 먼저 전송)", 
                         roomCode, chatRoom.getCurrentState());
                
                return ChatMessageMapper.toSendResponse(savedMessage, roomCode);
            } else {
                throw new RuntimeException("채팅방을 찾을 수 없습니다: " + roomCode);
            }
            
        } catch (Exception e) {
            log.error("관리자 연결 요청 실패 - roomCode: {}", roomCode, e);
            throw new RuntimeException("관리자 연결 요청에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public MessageResponse cancelAdminHandoff(String roomCode) {
        try {
            Optional<ChatRoom> chatRoomOpt = chatDataFacade.getChatRoom(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 🔄 명시적으로 AI_ACTIVE 상태로 전환 (확실하게!)
                chatRoom.stopWaitingForAdmin(); // waitingForAdmin = false
                chatRoom.transitionToState(ChatRoom.ChatRoomState.AI_ACTIVE); // currentState = AI_ACTIVE
                
                chatDataFacade.saveChatRoom(chatRoom);
                
                // 🗑️ Redis 캐시 무효화 (중요!)
                chatDataFacade.cacheChatRoom(roomCode, null); // null로 설정하여 캐시 무효화
                log.info("🗑️ 상담원 연결 요청 취소 시 Redis 캐시 무효화 완료 - roomCode: {}", roomCode);
                
                ChatMessage savedMessage = chatDataFacade.saveChatMessage(
                        roomCode,  AI_SENDER_ID, MessageSenderType.AI.name(), AI_SENDER_NAME,
                        "찍찍! 상담원 연결 요청을 취소했어요. 제가 계속 도와드리겠습니다!"
                );
                
                log.info("관리자 연결 요청 취소 완료 - roomCode: {}", roomCode);
                return ChatMessageMapper.toSendResponse(savedMessage, roomCode);
            } else {
                throw new RuntimeException("채팅방을 찾을 수 없습니다: " + roomCode);
            }
        } catch (Exception e) {
            log.error("관리자 연결 요청 취소 실패 - roomCode: {}", roomCode, e);
            throw new RuntimeException("관리자 연결 요청 취소에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public MessageResponse requestAIReturn(String roomCode) {
        try {
            Optional<ChatRoom> chatRoomOpt = chatDataFacade.getChatRoom(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 🔄 관리자 해제 및 AI 복귀 (명시적 상태 전환)
                chatRoom.releaseAdmin(); // currentState = AI_ACTIVE로 변경됨
                chatRoom.stopWaitingForAdmin(); // waitingForAdmin = false로 변경됨
                
                // 🆕 명시적으로 AI_ACTIVE 상태로 전환 (확실하게!)
                chatRoom.transitionToState(ChatRoom.ChatRoomState.AI_ACTIVE);
                
                ChatRoom savedRoom = chatDataFacade.saveChatRoom(chatRoom);
                
                // 🗑️ Redis 캐시 무효화 (중요!)
                chatDataFacade.cacheChatRoom(roomCode, null); // null로 설정하여 캐시 무효화
                log.info("🗑️ AI 복귀 시 Redis 캐시 무효화 완료 - roomCode: {}", roomCode);
                
                // HANDOFF-TO-AI SYSTEM MESSAGE (persistent) - 타입을 구분하여 저장
                ChatMessage savedSystemMessage = chatDataFacade.saveChatMessage(
                        roomCode, SYSTEM_SENDER_ID, MessageSenderType.SYSTEM.name(), SYSTEM_SENDER_NAME,
                        "HANDOFF_TO_AI:AI가 상담을 이어받습니다"
                );
                
                // Broadcast system message
                Map<String, Object> systemMessagePayload = Map.of(
                    "type", "HANDOFF_TO_AI",
                    "roomCode", roomCode,
                    "timestamp", LocalDateTime.now().toString(),
                    "message", "AI가 상담을 이어받습니다. 언제든 도움이 필요하시면 말씀해주세요.",
                    "messageId", savedSystemMessage.getId()
                );
                
                Map<String, Object> roomState = createRoomStateInfo(savedRoom, "handoff_to_ai");
                Map<String, Object> broadcastMessage = Map.of(
                    "type", "SYSTEM_MESSAGE",
                    "payload", systemMessagePayload,
                    "roomState", roomState
                );
                
                messagingTemplate.convertAndSend("/topic/chat/" + roomCode, broadcastMessage);
                
                // AI 복귀 메시지 생성
                ChatMessage savedMessage = chatDataFacade.saveChatMessage(
                        roomCode, AI_SENDER_ID, MessageSenderType.AI.name(), AI_SENDER_NAME,
                        "찍찍! 다시 제가 도와드리게 되었어요. 어떤 도움이 필요하신가요?"
                );
                
                log.info("✅ AI 복귀 요청 완료 - roomCode: {}, finalState: {}", roomCode, savedRoom.getCurrentState());
                return ChatMessageMapper.toSendResponse(savedMessage, roomCode);
            } else {
                throw new RuntimeException("채팅방을 찾을 수 없습니다: " + roomCode);
            }
        } catch (Exception e) {
            log.error("❌ AI 복귀 요청 실패 - roomCode: {}", roomCode, e);
            throw new RuntimeException("AI 복귀 요청에 실패했습니다.", e);
        }
    }

    /**
     * 대화 이력을 문자열로 변환
     */
    private String buildConversationHistory(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return "새로운 대화입니다.";
        }
        
        StringBuilder history = new StringBuilder();
        
        // 메시지를 시간순으로 정렬 (오래된 것부터)
        messages.stream()
            .sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
            .forEach(msg -> {
                String senderLabel = MessageSenderType.AI.name().equals(msg.getSenderType()) 
                    ? "AI" : "사용자";
                history.append(String.format("%s: %s\n", senderLabel, msg.getContent()));
            });
        
        return history.toString();
    }

    /**
     * 사람 상담 필요 여부 감지
     */
    private boolean detectNeedForHumanAssistance(String userMessage, List<ChatMessage> recentMessages) {
        try {
            String message = userMessage.toLowerCase();
            
            // 1. 명시적 키워드 감지 (강한 신호) - 진짜 문제 상황만
            String[] strongKeywords = {
                "결제", "환불", "취소", "계좌", "카드", "billing", "payment", 
                "오류", "에러", "버그", "작동", "안됨", "문제",
                "불만", "항의", "컴플레인", "complaint",
                "법적", "소송", "변호사", "legal",
                "사람", "상담원", "담당자", "직원", "매니저", "human", "person", "staff", "manager"
                // "어디", "언제", "누가" 등 일반적인 의문사는 제거 - AI가 충분히 답변 가능
            };
            
            for (String keyword : strongKeywords) {
                if (message.contains(keyword)) {
                    return true;
                }
            }
            
            // 2. 반복적 문의 감지 (같은 문제를 3번 이상 물어봄)
            long sameTopicCount = recentMessages.stream()
                .filter(msg -> "USER".equals(msg.getSenderType()))
                .limit(6) // 최근 6개 사용자 메시지 확인
                .mapToLong(msg -> {
                    String content = msg.getContent().toLowerCase();
                    // 동일 주제 키워드 검사
                    for (String keyword : strongKeywords) {
                        if (content.contains(keyword) && message.contains(keyword)) {
                            return 1;
                        }
                    }
                    return 0;
                })
                .sum();
                
            if (sameTopicCount >= 3) {
                return true;
            }
            
            // 3. 복잡성 감지 (긴 메시지 + 복잡한 상황 설명)
            if (message.length() > 100 && 
                (message.contains("여러") || message.contains("계속") || 
                 message.contains("몇번") || message.contains("자꾸"))) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.warn("사람 상담 필요성 감지 실패 - userMessage: {}", userMessage, e);
            return false;
        }
    }
    
    /**
     * AI가 사용자 메시지를 읽었음을 알리는 읽음 상태 업데이트
     */
    public void sendAIReadStatusUpdate(String roomCode) {
        try {
            // 1. 실제 데이터베이스의 readStatusJson 업데이트
            Optional<ChatRoom> chatRoomOpt = chatDataFacade.getChatRoom(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 가장 최근 메시지 ID 조회
                List<ChatMessage> recentMessages = chatDataFacade.getRecentMessages(roomCode);
                if (!recentMessages.isEmpty()) {
                    String latestMessageId = recentMessages.get(0).getId();
                    
                    // readStatusJson에 AI 읽음 상태 업데이트
                    String currentReadStatus = chatRoom.getReadStatusJson();
                    String updatedReadStatus = updateReadStatusForAI(currentReadStatus, latestMessageId);
                    chatRoom.updateReadStatus(updatedReadStatus);
                    chatDataFacade.saveChatRoom(chatRoom);
                    
                    log.debug("AI 읽음 상태 데이터베이스 업데이트 완료 - roomCode: {}, messageId: {}", roomCode, latestMessageId);
                }
            }
            
            // 2. WebSocket 브로드캐스트
            Map<String, Object> readStatusPayload = Map.of(
                "readerType", "AI",
                "unreadCount", 0,
                "roomCode", roomCode
            );

            Map<String, Object> broadcastMessage = Map.of(
                "type", "read_status_update",
                "payload", readStatusPayload
            );

            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomCode,
                broadcastMessage
            );
            
            log.debug("AI 읽음 상태 업데이트 완료 - roomCode: {}", roomCode);
            
        } catch (Exception e) {
            log.error("AI 읽음 상태 업데이트 실패 - roomCode: {}", roomCode, e);
        }
    }
    
    /**
     * AI 읽음 상태 업데이트를 위한 JSON 처리
     */
    private String updateReadStatusForAI(String currentReadStatus, String lastReadMessageId) {
        if (currentReadStatus == null || currentReadStatus.isEmpty() || currentReadStatus.equals("{}")) {
            return "{\"AI\":\"" + lastReadMessageId + "\"}";
        }
        
        // 기존 AI 정보가 있으면 업데이트, 없으면 추가
        if (currentReadStatus.contains("\"AI\"")) {
            return currentReadStatus.replaceAll("\"AI\":\"[^\"]*\"", "\"AI\":\"" + lastReadMessageId + "\"");
        } else {
            return currentReadStatus.substring(0, currentReadStatus.length() - 1) + 
                   ",\"AI\":\"" + lastReadMessageId + "\"}";
        }
    }
    
    /**
     * Unified message broadcasting for both AI and operator modes
     * Ensures consistent message format across all chat participants
     */
    private void broadcastUnifiedMessage(ChatMessage message, String roomCode, String messageType) {
        try {
            Map<String, Object> payload = Map.of(
                "roomId", roomCode,
                "messageId", message.getId(),
                "senderId", message.getSenderId(),
                "senderType", message.getSenderType(),
                "senderName", message.getSenderName(),
                "content", message.getContent(),
                "sentAt", message.getSentAt().toString()
            );

            Map<String, Object> broadcastMessage = Map.of(
                "type", messageType,
                "payload", payload
            );

            String channel = "/topic/chat/" + roomCode;
            messagingTemplate.convertAndSend(channel, broadcastMessage);
            
            log.info("📡 Unified message broadcast completed - roomCode: {}, type: {}, messageId: {}, channel: {}", 
                roomCode, messageType, message.getId(), channel);
            
        } catch (Exception e) {
            log.error("❌ Unified message broadcast failed - roomCode: {}, type: {}, messageId: {}", 
                roomCode, messageType, message.getId(), e);
        }
    }
    
    /**
     * Broadcast admin assignment update to all participants
     */
    private void broadcastAdminAssignmentUpdate(String roomCode, ChatRoom chatRoom) {
        try {
            if (chatRoom.hasAssignedAdmin()) {
                Map<String, Object> assignmentPayload = Map.of(
                    "roomCode", roomCode,
                    "currentAdminCode", chatRoom.getCurrentAdminCode(),
                    "adminDisplayName", chatRoom.getAdminDisplayName(),
                    "hasAssignedAdmin", true
                );

                Map<String, Object> assignmentMessage = Map.of(
                    "type", "ADMIN_ASSIGNMENT_UPDATE",
                    "payload", assignmentPayload
                );

                String channel = "/topic/chat/" + roomCode;
                messagingTemplate.convertAndSend(channel, assignmentMessage);
                
                log.info("📡 Admin assignment update broadcast - roomCode: {}, adminCode: {}", 
                    roomCode, chatRoom.getCurrentAdminCode());
            }
        } catch (Exception e) {
            log.error("❌ Admin assignment update broadcast failed - roomCode: {}", roomCode, e);
        }
    }

    /**
     * AI 메시지 WebSocket 브로드캐스트
     */
    private void broadcastAIMessage(ChatMessage aiMessage, String roomCode) {
        try {
            Map<String, Object> payload = Map.of(
                "roomId", roomCode,
                "messageId", aiMessage.getId(),
                "senderId", aiMessage.getSenderId(),
                "senderType", aiMessage.getSenderType(),
                "senderName", aiMessage.getSenderName(),
                "content", aiMessage.getContent(),
                "sentAt", aiMessage.getSentAt().toString()
            );

            Map<String, Object> broadcastMessage = Map.of(
                "type", "AI_MESSAGE",
                "payload", payload
            );

            String channel = "/topic/chat/" + roomCode;
            messagingTemplate.convertAndSend(channel, broadcastMessage);
            
            log.warn("🔊 AI 메시지 WebSocket 브로드캐스트 완료 - roomCode: {}, messageId: {}, channel: {}, content: '{}'", 
                roomCode, aiMessage.getId(), channel, aiMessage.getContent().substring(0, Math.min(50, aiMessage.getContent().length())));
            
        } catch (Exception e) {
            log.error("AI 메시지 WebSocket 브로드캐스트 실패 - roomCode: {}, messageId: {}", 
                roomCode, aiMessage.getId(), e);
        }
    }

    /**
     * 버튼 상태 업데이트 WebSocket 브로드캐스트
     */
    private void broadcastButtonStateUpdate(String roomCode, String newState) {
        try {
            Map<String, Object> statePayload = Map.of(
                "roomId", roomCode,
                "state", newState,
                "buttonText", getButtonText(newState),
                "buttonAction", getButtonAction(newState)
            );

            Map<String, Object> stateBroadcast = Map.of(
                "type", "BUTTON_STATE_UPDATE",
                "payload", statePayload
            );

            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomCode,
                stateBroadcast
            );
            
            log.debug("버튼 상태 업데이트 브로드캐스트 완료 - roomCode: {}, state: {}", roomCode, newState);
            
        } catch (Exception e) {
            log.error("버튼 상태 업데이트 브로드캐스트 실패 - roomCode: {}, state: {}", roomCode, newState, e);
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
     * 채팅방 상태 정보 생성 (WebSocket 메시지에 포함)
     */
    private Map<String, Object> createRoomStateInfo(ChatRoom chatRoom, String transitionReason) {
        if (chatRoom == null) {
            return Map.of(
                "current", "AI_ACTIVE",
                "description", "AI 상담 중",
                "buttonText", "Request Human",
                "timestamp", LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "unknown"
            );
        }
        
        ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
        Map<String, Object> stateInfo = Map.of(
            "current", currentState.name(),
            "description", currentState.getDescription(),
            "buttonText", currentState.getButtonText(),
            "timestamp", LocalDateTime.now().toString(),
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
                "timestamp", LocalDateTime.now().toString(),
                "transitionReason", transitionReason != null ? transitionReason : "message_flow",
                "adminInfo", adminInfo
            );
        }
        
        return stateInfo;
    }
}