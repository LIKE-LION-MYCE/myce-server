package com.myce.chat.service.impl;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.chat.type.MessageSenderType;
import com.myce.common.dto.PageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
        
        // MongoDB에서 페이징된 메시지 조회 (최신 순)
        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomCodeOrderBySentAtDesc(roomCode, pageable);
        
        // ChatMessage -> MessageResponse 변환 (읽음 상태 계산 포함)
        List<MessageResponse> messageResponses = messagePage.getContent().stream()
            .map(message -> {
                Integer unreadCount = calculateMessageUnreadCount(message);
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
                if (userLastReadId == null || message.getId().compareTo(userLastReadId) > 0) {
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
                    if (adminLastReadId == null || message.getId().compareTo(adminLastReadId) > 0) {
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