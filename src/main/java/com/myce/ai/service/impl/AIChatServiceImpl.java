package com.myce.ai.service.impl;

import com.myce.ai.service.AIChatService;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.MessageSenderType;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
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
import java.util.stream.Collectors;

/**
 * AI 채팅 서비스 구현체
 * 
 * AWS Bedrock Nova Lite 기반 플랫폼 상담 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    // AI 식별 상수
    private static final String PLATFORM_ROOM_PREFIX = "platform-";
    private static final String AI_SENDER_NAME = "찍찍봇 (AI 챗봇)";
    private static final Long AI_SENDER_ID = -1L;
    
    /**
     * 사용자별 컨텍스트 정보
     */
    private record UserContext(
        String userName,
        String membershipLevel, 
        List<String> recentReservations,
        String paymentStatus,
        Long userId
    ) {}

    /**
     * 공개 플랫폼 정보
     */
    private record PublicContext(
        List<String> availableExpos,
        String platformInfo,
        String pricingInfo
    ) {}
    
    // 의존성 주입
    private final ChatClient chatClient;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCacheService chatCacheService;
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String generateAIResponse(String userMessage, String roomCode) {
        try {
            // 1. 채팅방 상태 확인
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            boolean isWaitingForAdmin = chatRoomOpt.map(ChatRoom::isWaitingForAdmin).orElse(false);
            
            // 2. 대화 이력 조회
            List<ChatMessage> allRecentMessages = chatMessageRepository
                .findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
            
            List<ChatMessage> recentMessages = allRecentMessages.stream()
                .limit(10)
                .toList();
            
            // 3. 컨텍스트 수집
            UserContext userContext = buildUserContext(roomCode);
            PublicContext publicContext = buildPublicContext();
            String conversationHistory = buildConversationHistory(recentMessages);
            
            // 4. 사람 상담 필요 여부 감지
            boolean shouldSuggestHuman = detectNeedForHumanAssistance(userMessage, recentMessages);
            
            // 5. AI 프롬프트 구성 (대기 상태 고려)
            String systemPrompt = createSystemPromptWithContext(userContext, publicContext, isWaitingForAdmin, shouldSuggestHuman);
            
            String aiPrompt = String.format("""
                %s
                
                대화 이력:
                %s
                
                사용자 메시지: %s
                
                사용자가 사용한 언어로 자연스럽게 답변해주세요:
                """, systemPrompt, conversationHistory, userMessage);
            
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
            
            // AI 메시지 생성 및 저장
            ChatMessage aiMessage = ChatMessage.builder()
                .roomCode(roomCode)
                .senderType(MessageSenderType.AI.name())
                .senderId(AI_SENDER_ID)
                .senderName(AI_SENDER_NAME)
                .content(aiResponse)
                .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(aiMessage);
            
            // AI 메시지를 Redis 캐시에도 추가 (새로고침 후에도 보이도록)
            chatCacheService.addMessageToCache(roomCode, savedMessage);
            
            // 채팅방 마지막 메시지 정보 업데이트
            updateChatRoomLastMessage(roomCode, savedMessage.getId(), aiResponse);
            
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
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                log.info("🔄 Starting handoff transaction - roomCode: {}, adminCode: {}, currentState: waiting={}, hasAdmin={}", 
                    roomCode, adminCode, chatRoom.isWaitingForAdmin(), chatRoom.hasAssignedAdmin());
                
                // STEP 1: IMMEDIATE STATE TRANSITION - Block AI responses first
                chatRoom.assignAdmin(adminCode);
                chatRoom.stopWaitingForAdmin();
                ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
                
                log.info("✅ Admin assigned and AI blocked - roomCode: {}, adminCode: {}, hasAdmin: {}", 
                    roomCode, adminCode, savedRoom.hasAssignedAdmin());
                
                // STEP 2: GENERATE AI SUMMARY (for system message only)
                String conversationSummary = generateConversationSummary(roomCode);
                
                // STEP 2.5: SEND HANDOFF-TO-OPERATOR SYSTEM MESSAGE (persistent)
                ChatMessage handoffSystemMessage = ChatMessage.builder()
                    .roomCode(roomCode)
                    .senderType("SYSTEM")
                    .senderId(-99L)
                    .senderName("시스템")
                    .content("HANDOFF_TO_OPERATOR:" + conversationSummary)
                    .build();
                
                ChatMessage savedSystemMessage = chatMessageRepository.save(handoffSystemMessage);
                
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
                
                
                log.info("🎯 Complete handoff workflow finished - roomCode: {}, adminCode: {}, finalState: hasAdmin={}", 
                    roomCode, adminCode, savedRoom.hasAssignedAdmin());
            }
        } catch (Exception e) {
            log.error("❌ AI handoff failed - roomCode: {}, adminCode: {}", roomCode, adminCode, e);
            throw new RuntimeException("관리자 handoff에 실패했습니다.", e);
        }
    }

    @Override
    public String generateConversationSummary(String roomCode) {
        try {
            // 전체 대화 이력 조회 (최근 50개)
            List<ChatMessage> allMessages = chatMessageRepository
                .findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
            
            if (allMessages.isEmpty()) {
                return "찍찍! 대화 내용이 없어 요약할 내용이 없습니다.";
            }
            
            // 시간순으로 정렬 (오래된 것부터)
            List<ChatMessage> sortedMessages = allMessages.stream()
                .sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
                .toList();
            
            // 사용자 컨텍스트 구성
            UserContext userContext = buildUserContext(roomCode);
            
            // 대화 이력을 문자열로 변환
            StringBuilder conversationLog = new StringBuilder();
            sortedMessages.forEach(msg -> {
                String senderLabel = MessageSenderType.AI.name().equals(msg.getSenderType()) 
                    ? "AI 상담사" : "고객";
                conversationLog.append(String.format("[%s] %s: %s\n", 
                    msg.getSentAt().toString(), senderLabel, msg.getContent()));
            });
            
            // AI 요약 프롬프트 구성 (사용자와 관리자 모두 볼 수 있도록 전문적이고 친화적으로)
            String summaryPrompt = String.format("""
                다음은 MYCE 플랫폼 AI 상담사와 고객(%s, %s 등급) 간의 대화 내용입니다.
                
                대화 내용:
                %s
                
                위 대화를 상담원 인계를 위해 요약해주세요. 고객도 함께 볼 수 있으므로 전문적이고 정중하게 작성해주세요:
                
                요약 형식:
                
                📋 상담 인계 요약
                
                💬 문의 내용: [고객의 주요 문의사항을 명확하고 간단하게]
                
                📝 현재 상황: [문제의 현재 상태나 시도한 해결책을 간단하게]
                
                🔍 추가 확인 필요: [상담원이 추가로 도와드려야 할 부분]
                
                ─────────────────────────────
                💡 고객님, 위 내용이 정확하지 않다면 상담원님께 직접 말씀해 주세요.
                
                간결하고 읽기 쉽게, 고객과 상담원 모두에게 도움이 되는 요약을 작성해주세요.
                """, 
                userContext.userName(), 
                userContext.membershipLevel(),
                conversationLog.toString()
            );
            
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
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 2. AI 대기 메시지 먼저 생성 및 저장 (상태 변경 전에!)
                ChatMessage waitingMessage = ChatMessage.builder()
                    .roomCode(roomCode)
                    .senderType(MessageSenderType.AI.name())
                    .senderId(AI_SENDER_ID)
                    .senderName(AI_SENDER_NAME)
                    .content("찍찍! 상담원을 찾고 있어요. 잠시만 기다려주세요! 그동안 다른 궁금한 점이 있으시면 언제든 말씀해주세요.")
                    .build();
                
                ChatMessage savedMessage = chatMessageRepository.save(waitingMessage);
                
                // 3. 채팅방 마지막 메시지 정보 업데이트
                updateChatRoomLastMessage(roomCode, savedMessage.getId(), savedMessage.getContent());
                
                // 4. 이제 채팅방 상태를 대기 상태로 변경 (AI 메시지 후에!)
                chatRoom.startWaitingForAdmin();
                chatRoomRepository.save(chatRoom);
                
                log.info("관리자 연결 요청 시작 - roomCode: {} (AI 메시지 먼저 전송)", roomCode);
                
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
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                chatRoom.stopWaitingForAdmin();
                chatRoomRepository.save(chatRoom);
                
                // 취소 확인 메시지 생성
                ChatMessage cancelMessage = ChatMessage.builder()
                    .roomCode(roomCode)
                    .senderType(MessageSenderType.AI.name())
                    .senderId(AI_SENDER_ID)
                    .senderName(AI_SENDER_NAME)
                    .content("찍찍! 상담원 연결 요청을 취소했어요. 제가 계속 도와드리겠습니다!")
                    .build();
                
                ChatMessage savedMessage = chatMessageRepository.save(cancelMessage);
                updateChatRoomLastMessage(roomCode, savedMessage.getId(), savedMessage.getContent());
                
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
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 관리자 해제 및 AI 복귀
                chatRoom.releaseAdmin();
                chatRoom.stopWaitingForAdmin();
                ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
                
                // HANDOFF-TO-AI SYSTEM MESSAGE (persistent)
                ChatMessage aiReturnSystemMessage = ChatMessage.builder()
                    .roomCode(roomCode)
                    .senderType("SYSTEM")
                    .senderId(-99L)
                    .senderName("시스템")
                    .content("HANDOFF_TO_AI:AI가 상담을 이어받습니다")
                    .build();
                
                ChatMessage savedSystemMessage = chatMessageRepository.save(aiReturnSystemMessage);
                
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
                ChatMessage returnMessage = ChatMessage.builder()
                    .roomCode(roomCode)
                    .senderType(MessageSenderType.AI.name())
                    .senderId(AI_SENDER_ID)
                    .senderName(AI_SENDER_NAME)
                    .content("찍찍! 다시 제가 도와드리게 되었어요. 어떤 도움이 필요하신가요?")
                    .build();
                
                ChatMessage savedMessage = chatMessageRepository.save(returnMessage);
                updateChatRoomLastMessage(roomCode, savedMessage.getId(), savedMessage.getContent());
                
                log.info("AI 복귀 요청 완료 - roomCode: {}", roomCode);
                return ChatMessageMapper.toSendResponse(savedMessage, roomCode);
            } else {
                throw new RuntimeException("채팅방을 찾을 수 없습니다: " + roomCode);
            }
        } catch (Exception e) {
            log.error("AI 복귀 요청 실패 - roomCode: {}", roomCode, e);
            throw new RuntimeException("AI 복귀 요청에 실패했습니다.", e);
        }
    }

    /**
     * 사용자별 컨텍스트 구성 (격리된 정보만 제공)
     */
    private UserContext buildUserContext(String roomCode) {
        try {
            // platform-{memberId}에서 memberId 추출
            Long userId = extractUserIdFromRoomCode(roomCode);
            
            // 사용자 기본 정보 조회
            Member user = memberRepository.findById(userId)
                .orElse(null);
            
            if (user == null) {
                return new UserContext("사용자", "일반", List.of(), "정보 없음", userId);
            }
            
            // TODO: 예약 정보, 결제 상태 등은 추후 구현
            List<String> recentReservations = List.of("예약 정보 조회 예정");
            String paymentStatus = "결제 상태 조회 예정";
            
            return new UserContext(
                user.getName(),
                user.getMemberGrade() != null ? user.getMemberGrade().getGradeCode().getName() : "일반",
                recentReservations,
                paymentStatus,
                userId
            );
            
        } catch (Exception e) {
            log.warn("사용자 컨텍스트 구성 실패 - roomCode: {}", roomCode, e);
            return new UserContext("사용자", "일반", List.of(), "정보 없음", -1L);
        }
    }

    /**
     * 공개 플랫폼 정보 구성
     */
    private PublicContext buildPublicContext() {
        try {
            // 공개 박람회 목록 조회 (상위 5개)
            List<Expo> publicExpos = expoRepository.findTop5ByOrderByCreatedAtDesc();
            List<String> expoTitles = publicExpos.stream()
                .map(Expo::getTitle)
                .collect(Collectors.toList());
            
            String platformInfo = """
                MYCE는 박람회 관리 플랫폼입니다.
                - 박람회 예약 및 관리
                - 티켓 구매 시스템  
                - 실시간 채팅 상담
                """;
                
            String pricingInfo = "요금제 정보는 개별 박람회마다 상이합니다.";
            
            return new PublicContext(expoTitles, platformInfo, pricingInfo);
            
        } catch (Exception e) {
            log.warn("공개 컨텍스트 구성 실패", e);
            return new PublicContext(List.of(), "플랫폼 정보 로딩 실패", "요금 정보 조회 불가");
        }
    }

    /**
     * roomCode에서 사용자 ID 추출
     */
    private Long extractUserIdFromRoomCode(String roomCode) {
        try {
            if (roomCode != null && roomCode.startsWith(PLATFORM_ROOM_PREFIX)) {
                String[] parts = roomCode.split("-");
                if (parts.length == 2) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("roomCode에서 사용자 ID 추출 실패: {}", roomCode);
        }
        throw new IllegalArgumentException("Invalid platform room code: " + roomCode);
    }

    /**
     * 컨텍스트 포함 AI 시스템 프롬프트 생성
     */
    private String createSystemPromptWithContext(UserContext userContext, PublicContext publicContext, boolean isWaitingForAdmin, boolean shouldSuggestHuman) {
        String waitingMessage = isWaitingForAdmin ? 
            "\n\n⏰ **현재 상태**: 상담원 연결 요청됨 - 대기 중 사용자와 소통하며 도움을 드리세요." : "";
        
        String humanSuggestionMessage = shouldSuggestHuman ? 
            "\n\n💡 **중요**: 이 문의는 전문 상담원의 도움이 필요해 보입니다. 답변 마지막에 '위 버튼을 눌러 상담원과 연결하시면 더 정확한 도움을 받으실 수 있어요!'라고 자연스럽게 안내해주세요." : "";
            
        return String.format("""
            당신은 MYCE 플랫폼의 AI 상담사 '찍찍킹'입니다.
            
            현재 상담 중인 사용자 정보:
            - 이름: %s
            - 회원 등급: %s  
            - 최근 예약: %s
            - 결제 상태: %s
            
            MYCE 플랫폼 정보:
            %s
            
            현재 이용 가능한 박람회:
            %s%s%s
            
            성격과 말투:
            - 한국어 존댓말을 사용하세요 (반말 금지)
            - 도움이 되고 정중한 태도를 유지하세요
            - 가끔 자연스럽게 '찍찍!'이나 '찍찍~' 같은 쥐 소리를 적절히 섞어서 말하세요
            - 너무 자주 사용하지 말고, 인사나 감탄할 때 적절히 사용하세요
            %s
            
            역할:
            - MYCE는 박람회 관리 플랫폼입니다
            - 사용자의 플랫폼 이용 문의에 도움을 드리세요
            - 박람회 예약, 계정 관리, 일반적인 질문에 답변하세요
            - 복잡한 기술적 문제나 결제 이슈는 전문 상담원이 더 도움이 될 수 있습니다
            - 사용자의 개인 정보를 바탕으로 맞춤형 상담을 제공하세요
            
            답변 가이드라인:
            - 300자 이내로 간결하게 답변하세요
            - 구체적이고 실용적인 정보를 제공하세요
            - 사용자 정보를 활용한 개인화된 정보를 제공하세요
            - 확실하지 않은 정보는 추측하지 마세요
            """, 
            userContext.userName(),
            userContext.membershipLevel(),
            String.join(", ", userContext.recentReservations()),
            userContext.paymentStatus(),
            publicContext.platformInfo(),
            String.join(", ", publicContext.availableExpos()),
            waitingMessage,
            humanSuggestionMessage,
            isWaitingForAdmin ? "- 상담원 연결 대기 중임을 자연스럽게 언급하고 계속 도움을 드리세요" : ""
        );
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
            
            // 1. 명시적 키워드 감지 (강한 신호)
            String[] strongKeywords = {
                "결제", "환불", "취소", "계좌", "카드", "billing", "payment", 
                "오류", "에러", "버그", "작동", "안됨", "문제",
                "불만", "항의", "컴플레인", "complaint",
                "법적", "소송", "변호사", "legal",
                "사람", "상담원", "담당자", "직원", "매니저", "human", "person", "staff", "manager",
                "where", "where's", "어디", "언제", "누가", "who"
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
    private void sendAIReadStatusUpdate(String roomCode) {
        try {
            // 1. 실제 데이터베이스의 readStatusJson 업데이트
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
            if (chatRoomOpt.isPresent()) {
                ChatRoom chatRoom = chatRoomOpt.get();
                
                // 가장 최근 메시지 ID 조회
                List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
                if (!recentMessages.isEmpty()) {
                    String latestMessageId = recentMessages.get(0).getId();
                    
                    // readStatusJson에 AI 읽음 상태 업데이트
                    String currentReadStatus = chatRoom.getReadStatusJson();
                    String updatedReadStatus = updateReadStatusForAI(currentReadStatus, latestMessageId);
                    chatRoom.updateReadStatus(updatedReadStatus);
                    chatRoomRepository.save(chatRoom);
                    
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
     * 채팅방 마지막 메시지 정보 업데이트
     */
    private void updateChatRoomLastMessage(String roomCode, String messageId, String content) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);
        
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatRoom.updateLastMessageInfo(messageId, content);
            chatRoomRepository.save(chatRoom);
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