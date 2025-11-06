package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.service.ChatUnreadService;
import com.myce.member.entity.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 채팅 읽지 않은 메시지 계산 통합 서비스 구현체
 * 
 * 핵심 원칙: 카카오톡 방식
 * - 내가 보낸 메시지 → 상대방이 읽었는가?
 * - 상대방이 보낸 메시지 → 내가 읽었는가?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUnreadServiceImpl implements ChatUnreadService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    
    @Override
    public Long getUnreadCountForViewer(String roomCode, Long viewerId, String viewerRole) {
        try {
            log.debug(" unread count 계산 시작 - roomCode: {}, viewerId: {}, viewerRole: {}", 
                     roomCode, viewerId, viewerRole);
            
            // 1. 채팅방 조회
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
            if (chatRoom == null) {
                log.warn("채팅방을 찾을 수 없음 - roomCode: {}", roomCode);
                return 0L;
            }
            
            // 2. readStatusJson에서 내가 마지막으로 읽은 메시지 ID 추출
            String readStatusJson = chatRoom.getReadStatusJson();
            String myUserType = determineUserType(viewerRole);
            String myLastReadMessageId = extractLastReadMessageId(readStatusJson, myUserType);
            
            log.debug(" 내 읽음 상태 - userType: {}, lastReadId: {}", myUserType, myLastReadMessageId);
            
            // 3. 내가 읽어야 할 메시지 타입 결정
            String targetSenderType = determineTargetSenderType(viewerRole, chatRoom);
            
            // 4. 읽지 않은 메시지 개수 계산
            Long unreadCount;
            if (myLastReadMessageId == null || myLastReadMessageId.isEmpty()) {
                // 아직 아무것도 읽지 않았다면 전체 상대방 메시지 개수
                unreadCount = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, targetSenderType);
                log.debug(" 아무것도 안읽음 - targetSenderType: {}, count: {}", targetSenderType, unreadCount);
            } else {
                // 마지막 읽은 메시지 이후의 상대방 메시지 개수
                unreadCount = chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                    roomCode, targetSenderType, myLastReadMessageId);
                log.debug(" 부분 읽음 - targetSenderType: {}, lastReadId: {}, count: {}", 
                         targetSenderType, myLastReadMessageId, unreadCount);
            }
            
            log.debug(" unread count 계산 완료 - roomCode: {}, viewerId: {}, count: {}", 
                     roomCode, viewerId, unreadCount);
            return unreadCount;
            
        } catch (Exception e) {
            log.error("unread count 계산 실패 - roomCode: {}, viewerId: {}", roomCode, viewerId, e);
            return 0L; // 에러 시 안전한 기본값 반환
        }
    }
    
    @Override
    public Integer getMessageUnreadCount(String messageId, Long messageSenderId, String messageSenderType, String roomCode) {
        try {
            log.debug(" 개별 메시지 unread count 계산 - messageId: {}, senderType: {}", messageId, messageSenderType);
            
            // 1. 채팅방 조회 (한 번만 조회하여 성능 최적화)
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
            if (chatRoom == null || messageId == null) {
                return 1; // 안전한 기본값
            }
            
            //  플랫폼 AI 채팅에서 USER 메시지는 항상 읽음으로 처리 (AI/관리자가 즉시 읽었다고 간주)
            if (roomCode != null && roomCode.startsWith("platform-") && "USER".equals(messageSenderType)) {
                ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
                if (currentState == ChatRoom.ChatRoomState.AI_ACTIVE || 
                    currentState == ChatRoom.ChatRoomState.ADMIN_ACTIVE) {
                    log.debug(" 플랫폼 상담 중 USER 메시지 - unreadCount 강제 0 설정 - messageId: {}, state: {}", 
                              messageId, currentState);
                    return 0;
                }
            }
            
            // 2. 메시지 발송자에 따라 읽음 상태 확인할 상대방 타입 결정
            String readerType = determineReaderType(messageSenderType, chatRoom);
            if (readerType == null) {
                return 0; // SYSTEM 메시지 등은 읽음 처리 안함
            }
            
            // 3. 상대방이 이 메시지를 읽었는지 확인
            String readerLastReadId = extractLastReadMessageId(chatRoom.getReadStatusJson(), readerType);
            
            if (readerLastReadId == null || readerLastReadId.isEmpty()) {
                // 상대방이 아무것도 안 읽음 → 안읽음
                return 1;
            }
            
            // 4. 메시지 ID 비교 (MongoDB ObjectId는 시간순 정렬 가능)
            boolean isRead = messageId.compareTo(readerLastReadId) <= 0;
            int result = isRead ? 0 : 1;
            
            log.debug(" 개별 메시지 읽음 상태 - messageId: {}, readerType: {}, readerLastRead: {}, isRead: {}", 
                     messageId, readerType, readerLastReadId, isRead);
            
            return result;
            
        } catch (Exception e) {
            log.warn("개별 메시지 unread count 계산 실패 - messageId: {}", messageId, e);
            return 1; // 에러 시 안읽음으로 표시
        }
    }
    
    @Override
    public Long getTotalUnreadBadgeCount(Long userId, String userRole) {
        try {
            log.debug(" 전체 배지 카운트 계산 - userId: {}, userRole: {}", userId, userRole);
            
            // 1. 사용자의 모든 활성 채팅방 조회
            List<ChatRoom> userRooms;
            
            if (Role.PLATFORM_ADMIN.name().equals(userRole)) {
                // 플랫폼 관리자: 모든 플랫폼 채팅방
                userRooms = chatRoomRepository.findByExpoIdIsNullAndIsActiveTrueOrderByLastMessageAtDesc();
            } else {
                // 일반 사용자 및 박람회 관리자: 본인이 참여한 채팅방
                userRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(userId);
            }
            
            // 2. 각 채팅방의 unread count 합산
            Long totalUnread = 0L;
            for (ChatRoom room : userRooms) {
                Long roomUnread = getUnreadCountForViewer(room.getRoomCode(), userId, userRole);
                totalUnread += roomUnread;
            }
            
            log.debug(" 전체 배지 카운트 계산 완료 - userId: {}, totalUnread: {}", userId, totalUnread);
            return totalUnread;
            
        } catch (Exception e) {
            log.error("전체 배지 카운트 계산 실패 - userId: {}", userId, e);
            return 0L;
        }
    }
    
    @Override
    public String extractLastReadMessageId(String readStatusJson, String userType) {
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
    
    // === 헬퍼 메서드들 ===
    
    /**
     * 사용자 역할에 따라 readStatusJson에서 사용할 userType 결정
     */
    private String determineUserType(String viewerRole) {
        if (Role.USER.name().equals(viewerRole)) {
            return "USER";
        } else {
            // PLATFORM_ADMIN, EXPO_ADMIN 모두 ADMIN으로 처리
            return "ADMIN"; 
        }
    }
    
    /**
     * 내 역할에 따라 내가 읽어야 할 메시지의 발송자 타입 결정
     */
    private String determineTargetSenderType(String viewerRole, ChatRoom chatRoom) {
        boolean isPlatformRoom = chatRoom.getExpoId() == null;
        
        if (Role.USER.name().equals(viewerRole)) {
            // 일반 사용자는 ADMIN/AI 메시지를 읽어야 함
            if (isPlatformRoom) {
                // 플랫폼 채팅방에서는 AI 또는 ADMIN 메시지 (둘 중 하나라도 있으면)
                // 일단 ADMIN으로 통일 (AI 메시지도 ADMIN 타입으로 저장되는 경우 많음)
                return "ADMIN";
            } else {
                // 박람회 채팅방에서는 ADMIN 메시지
                return "ADMIN";
            }
        } else {
            // 관리자는 USER 메시지를 읽어야 함
            return "USER";
        }
    }
    
    /**
     * 메시지 발송자 타입에 따라 읽음 상태를 확인할 상대방 타입 결정
     */
    private String determineReaderType(String messageSenderType, ChatRoom chatRoom) {
        if ("SYSTEM".equals(messageSenderType)) {
            return null; // 시스템 메시지는 읽음 처리 안함
        }
        
        if ("USER".equals(messageSenderType)) {
            // USER가 보낸 메시지 → ADMIN이 읽어야 함
            return "ADMIN";
        } else if ("ADMIN".equals(messageSenderType) || "AI".equals(messageSenderType)) {
            // ADMIN/AI가 보낸 메시지 → USER가 읽어야 함
            return "USER";
        }
        
        return null;
    }
    
}