package com.myce.chat.service.impl;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.MessageSenderType;
import com.myce.common.dto.PageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 채팅 메시지 생성 서비스 구현체
 * <p>
 * 메시지 생성 로직을 중앙화하여 일관성 보장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatCacheService chatCacheService;
    private final ChatUnreadService chatUnreadService;

    /**
     * 기본 메시지 타입
     */
    private static final String DEFAULT_MESSAGE_TYPE = "TEXT";
    private static final String EMPTY_JSON = "{}";
    private static final Long SYSTEM_SENDER_ID = 0L;

    /**
     * 시스템 메시지 타입
     */
    private static final String SYSTEM_ENTER_TYPE = "SYSTEM_ENTER";
    private static final String SYSTEM_LEAVE_TYPE = "SYSTEM_LEAVE";

    /**
     * 시스템 메시지 포맷
     */
    private static final String ENTER_MESSAGE_FORMAT = "%s님이 채팅방에 입장하셨습니다.";
    private static final String LEAVE_MESSAGE_FORMAT = "%s님이 채팅방을 나가셨습니다.";

    @Override
    public ChatMessage createMessage(String roomCode, String senderType, Long senderId, 
                                   String senderName, String content) {
        return createMessage(roomCode, senderType, senderId, senderName, content, 
                           false, DEFAULT_MESSAGE_TYPE, null);
    }

    @Override
    public ChatMessage createFileMessage(String roomCode, String senderType, Long senderId,
                                       String senderName, String content, String messageType, 
                                       String fileInfoJson) {
        return createMessage(roomCode, senderType, senderId, senderName, content,
                false, messageType, fileInfoJson);
    }

    @Override
    public ChatMessage createSystemMessage(String roomCode, String messageType, String content) {
        return createMessage(roomCode, MessageSenderType.SYSTEM.name(), SYSTEM_SENDER_ID,
                MessageSenderType.SYSTEM.getDescription(), content, true, messageType, null);
    }

    @Override
    public ChatMessage createEnterMessage(String roomCode, String memberName) {
        String content = String.format(ENTER_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, SYSTEM_ENTER_TYPE, content);
    }

    @Override
    public ChatMessage createLeaveMessage(String roomCode, String memberName) {
        String content = String.format(LEAVE_MESSAGE_FORMAT, memberName);
        return createSystemMessage(roomCode, SYSTEM_LEAVE_TYPE, content);
    }

    @Override
    public PageResponse<MessageResponse> getMessages(String roomCode, Pageable pageable) {
        
        // 첫 페이지이고 크기가 50 이하인 경우 Redis 캐시 확인
        if (pageable.getPageNumber() == 0 && pageable.getPageSize() <= 50) {
            List<ChatMessage> cachedMessages = chatCacheService.getCachedRecentMessages(roomCode, pageable.getPageSize());
            
            if (cachedMessages != null && !cachedMessages.isEmpty()) {
                // 캐시 히트 - Redis에서 데이터 반환 (5-10ms)
                log.debug("Cache hit for room: {}, returning {} messages from Redis", roomCode, cachedMessages.size());
                
                // ChatRoom 캐시 조회 → DB 폴백 (성능 최적화)
                long chatRoomStartTime = System.currentTimeMillis();
                ChatRoom cachedChatRoom = chatCacheService.getCachedChatRoom(roomCode);
                final ChatRoom finalChatRoom;
                if (cachedChatRoom != null) {
                    finalChatRoom = cachedChatRoom;
                } else {
                    // 캐시 미스 시 DB 조회 후 캐싱
                    ChatRoom dbChatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
                    if (dbChatRoom != null) {
                        chatCacheService.cacheChatRoom(roomCode, dbChatRoom);
                    }
                    finalChatRoom = dbChatRoom;
                }
                long chatRoomEndTime = System.currentTimeMillis();
                log.debug(" Redis경로 ChatRoom 조회 시간: {}ms - roomCode: {}", (chatRoomEndTime - chatRoomStartTime), roomCode);
                
                // 성능 최적화: ChatRoom 재사용으로 N+1 쿼리 방지
                List<MessageResponse> messageResponses = cachedMessages.stream()
                    .map(message -> {
                        // 최적화된 메서드 사용 - ChatRoom을 재사용하여 MongoDB 조회 제거
                        Integer unreadCount = calculateMessageUnreadCountOptimized(message, finalChatRoom);
                        return ChatMessageMapper.toDto(message, unreadCount);
                    })
                    .toList();
                
                return new PageResponse<>(
                    messageResponses,
                    0,
                    cachedMessages.size(),
                    (long) cachedMessages.size(),
                    1
                );
            }
        }
        
        // 캐시 미스 또는 첫 페이지가 아닌 경우 - MongoDB 조회
        log.debug("Cache miss or not first page for room: {}, fetching from MongoDB", roomCode);
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomCodeOrderBySentAtDesc(roomCode, pageable);
        
        // 첫 페이지인 경우 Redis에 캐싱 (50개 메시지, 7일 TTL)
        if (pageable.getPageNumber() == 0 && !messagePage.getContent().isEmpty()) {
            chatCacheService.cacheRecentMessages(roomCode, messagePage.getContent());
            log.debug("Cached {} messages for room: {}", messagePage.getContent().size(), roomCode);
        }
        
        // ChatRoom 캐시 조회 → DB 폴백 (성능 최적화)
        long chatRoomStartTime = System.currentTimeMillis();
        ChatRoom cachedChatRoom = chatCacheService.getCachedChatRoom(roomCode);
        final ChatRoom finalChatRoom;
        if (cachedChatRoom != null) {
            finalChatRoom = cachedChatRoom;
        } else {
            // 캐시 미스 시 DB 조회 후 캐싱
            ChatRoom dbChatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
            if (dbChatRoom != null) {
                chatCacheService.cacheChatRoom(roomCode, dbChatRoom);
            }
            finalChatRoom = dbChatRoom;
        }
        long chatRoomEndTime = System.currentTimeMillis();
        log.debug(" MongoDB경로 ChatRoom 조회 시간: {}ms - roomCode: {}", (chatRoomEndTime - chatRoomStartTime), roomCode);
        
        // 성능 최적화: ChatRoom 재사용으로 N+1 쿼리 방지
        List<MessageResponse> messageResponses = messagePage.getContent().stream()
            .map(message -> {
                // 최적화된 메서드 사용 - ChatRoom을 재사용하여 MongoDB 조회 제거
                Integer unreadCount = calculateMessageUnreadCountOptimized(message, finalChatRoom);
                return ChatMessageMapper.toDto(message, unreadCount);
            })
            .toList();
        
        
        return new PageResponse<>(
                messageResponses,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages()
        );
    }

    /**
     * 메시지 생성 핵심 로직
     */
    private ChatMessage createMessage(String roomCode, String senderType, Long senderId,
            String senderName, String content, Boolean isSystemMessage,
            String messageType, String fileInfoJson) {
        return ChatMessage.builder()
                .roomCode(roomCode)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .isSystemMessage(isSystemMessage != null ? isSystemMessage : false)
                .messageType(messageType != null ? messageType : DEFAULT_MESSAGE_TYPE)
                .fileInfoJson(fileInfoJson)
                .build();
    }
    
    /**
     * 개별 메시지의 읽지 않은 수 계산 (카카오톡 스타일)
     * 플랫폼 채팅방은 AI 읽음 상태도 고려, 일반 채팅방은 기존 로직 유지
     */
    private Integer calculateMessageUnreadCount(ChatMessage message) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(message.getRoomCode()).orElse(null);
            if (chatRoom == null) {
                return 1; // 채팅방이 없으면 안읽음으로 표시
            }
            
            String readStatusJson = chatRoom.getReadStatusJson();
            boolean isPlatformRoom = message.getRoomCode() != null && message.getRoomCode().startsWith("platform-");
            
            // 메시지 발송자에 따라 상대방의 읽음 상태 확인
            if ("ADMIN".equals(message.getSenderType()) || "AI".equals(message.getSenderType())) {
                // 관리자나 AI가 보낸 메시지 -> 사용자가 읽었는지 확인
                String userLastReadId = extractLastReadMessageId(readStatusJson, "USER");
                boolean isUnread = userLastReadId == null || message.getId().compareTo(userLastReadId) > 0;
                
                    
                if (isUnread) {
                    return 1; // 사용자가 안 읽음
                }
            } else {
                // 사용자가 보낸 메시지 -> 상대방이 읽었는지 확인
                if (isPlatformRoom) {
                    // 플랫폼 채팅방: AI 또는 관리자 중 하나라도 읽었으면 읽음 처리
                    String aiLastReadId = extractLastReadMessageId(readStatusJson, "AI");
                    String adminLastReadId = extractLastReadMessageId(readStatusJson, "ADMIN");
                    
                    boolean aiRead = aiLastReadId != null && message.getId().compareTo(aiLastReadId) <= 0;
                    boolean adminRead = adminLastReadId != null && message.getId().compareTo(adminLastReadId) <= 0;
                    
                    if (!aiRead && !adminRead) {
                        return 1; // AI도 관리자도 안 읽음
                    }
                } else {
                    // 일반 채팅방 (expo 포함): 관리자 읽음 상태만 확인 (기존 로직 유지)
                    String adminLastReadId = extractLastReadMessageId(readStatusJson, "ADMIN");
                    boolean isUnread = adminLastReadId == null || message.getId().compareTo(adminLastReadId) > 0;
                    
                        
                    if (isUnread) {
                        return 1; // 관리자가 안 읽음
                    }
                }
            }
            
            return 0; // 읽음
        } catch (Exception e) {
            log.warn("메시지 읽음 상태 계산 실패 - messageId: {}", message.getId());
            return 1; // 에러시 안읽음으로 표시
        }
    }
    
    /**
     * 최적화된 개별 메시지의 읽지 않은 수 계산 (ChatRoom 재사용)
     * 성능 개선: N번 DB 조회 → 1번으로 감소
     */
    private Integer calculateMessageUnreadCountOptimized(ChatMessage message, ChatRoom chatRoom) {
        try {
            if (chatRoom == null) {
                return 1; // 채팅방이 없으면 안읽음으로 표시
            }
            
            // messageId가 null인 경우 처리
            if (message.getId() == null) {
                log.warn("메시지 ID가 null입니다 - roomCode: {}, content: {}", message.getRoomCode(), 
                    message.getContent() != null ? message.getContent().substring(0, Math.min(message.getContent().length(), 20)) : "null");
                return 1; // 기본값으로 안읽음 처리
            }
            
            String readStatusJson = chatRoom.getReadStatusJson();
            boolean isPlatformRoom = message.getRoomCode() != null && message.getRoomCode().startsWith("platform-");
            
            // 메시지 발송자에 따라 상대방의 읽음 상태 확인
            if ("ADMIN".equals(message.getSenderType()) || "AI".equals(message.getSenderType())) {
                // 관리자나 AI가 보낸 메시지 -> 사용자가 읽었는지 확인
                String userLastReadId = extractLastReadMessageId(readStatusJson, "USER");
                boolean isUnread = userLastReadId == null || message.getId().compareTo(userLastReadId) > 0;
                
                if (isUnread) {
                    return 1; // 사용자가 안 읽음
                }
            } else {
                // 사용자가 보낸 메시지 -> 상대방이 읽었는지 확인
                if (isPlatformRoom) {
                    // 플랫폼 채팅방: AI 또는 관리자 중 하나라도 읽었으면 읽음 처리
                    String aiLastReadId = extractLastReadMessageId(readStatusJson, "AI");
                    String adminLastReadId = extractLastReadMessageId(readStatusJson, "ADMIN");
                    
                    boolean aiRead = aiLastReadId != null && message.getId().compareTo(aiLastReadId) <= 0;
                    boolean adminRead = adminLastReadId != null && message.getId().compareTo(adminLastReadId) <= 0;
                    
                    if (!aiRead && !adminRead) {
                        return 1; // AI도 관리자도 안 읽음
                    }
                } else {
                    // 일반 채팅방 (expo 포함): 관리자 읽음 상태만 확인 (기존 로직 유지)
                    String adminLastReadId = extractLastReadMessageId(readStatusJson, "ADMIN");
                    boolean isUnread = adminLastReadId == null || message.getId().compareTo(adminLastReadId) > 0;
                    
                    if (isUnread) {
                        return 1; // 관리자가 안 읽음
                    }
                }
            }
            
            return 0; // 읽음
        } catch (Exception e) {
            log.warn("최적화된 메시지 읽음 상태 계산 실패 - messageId: {}", message.getId());
            return 1; // 에러시 안읽음으로 표시
        }
    }
    
    /**
     * readStatusJson을 한번만 파싱하여 모든 사용자 타입의 마지막 읽은 메시지 ID를 추출 (배치 최적화)
     */
    private Map<String, String> parseReadStatusOnce(ChatRoom chatRoom) {
        Map<String, String> result = new HashMap<>();
        
        try {
            if (chatRoom == null) {
                return result;
            }
            
            String readStatusJson = chatRoom.getReadStatusJson();
            if (readStatusJson == null || readStatusJson.isEmpty() || readStatusJson.equals("{}")) {
                return result;
            }
            
            // 모든 사용자 타입을 한 번에 파싱
            String[] userTypes = {"USER", "ADMIN", "AI"};
            for (String userType : userTypes) {
                String searchKey = "\"" + userType + "\":\"";
                int startIndex = readStatusJson.indexOf(searchKey);
                if (startIndex != -1) {
                    startIndex += searchKey.length();
                    int endIndex = readStatusJson.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        result.put(userType, readStatusJson.substring(startIndex, endIndex));
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("readStatusJson 배치 파싱 실패: {}", chatRoom.getRoomCode(), e);
        }
        
        return result;
    }
    
    /**
     * 최적화된 메시지 읽음 상태 계산 (배치 처리)
     * parseReadStatusOnce로 미리 파싱된 데이터 사용
     */
    private Integer calculateMessageUnreadCountBatch(ChatMessage message, ChatRoom chatRoom, Map<String, String> parsedReadStatus) {
        try {
            if (chatRoom == null || message.getId() == null) {
                return 1;
            }
            
            boolean isPlatformRoom = message.getRoomCode() != null && message.getRoomCode().startsWith("platform-");
            
            // 메시지 발송자에 따라 상대방의 읽음 상태 확인
            if ("ADMIN".equals(message.getSenderType()) || "AI".equals(message.getSenderType())) {
                // 관리자나 AI가 보낸 메시지 -> 사용자가 읽었는지 확인
                String userLastReadId = parsedReadStatus.get("USER");
                boolean isRead = userLastReadId != null && message.getId().compareTo(userLastReadId) <= 0;
                return isRead ? 0 : 1;
                
            } else {
                // 사용자가 보낸 메시지 -> 상대방이 읽었는지 확인
                if (isPlatformRoom) {
                    // 플랫폼 채팅방: AI 또는 관리자 중 하나라도 읽었으면 읽음 처리
                    String aiLastReadId = parsedReadStatus.get("AI");
                    String adminLastReadId = parsedReadStatus.get("ADMIN");
                    
                    boolean aiRead = aiLastReadId != null && message.getId().compareTo(aiLastReadId) <= 0;
                    boolean adminRead = adminLastReadId != null && message.getId().compareTo(adminLastReadId) <= 0;
                    
                    boolean isRead = aiRead || adminRead;
                    return isRead ? 0 : 1;
                } else {
                    // 일반 채팅방 (expo 포함): 관리자 읽음 상태만 확인
                    String adminLastReadId = parsedReadStatus.get("ADMIN");
                    boolean isRead = adminLastReadId != null && message.getId().compareTo(adminLastReadId) <= 0;
                    return isRead ? 0 : 1;
                }
            }
            
        } catch (Exception e) {
            log.warn("배치 메시지 읽음 상태 계산 실패 - messageId: {}", message.getId());
            return 1; // 에러시 안읽음으로 표시
        }
    }
    
    /**
     * readStatusJson에서 특정 타입의 마지막 읽은 메시지 ID 추출
     * ExpoChatServiceImpl의 extractLastReadMessageId 로직과 동일
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