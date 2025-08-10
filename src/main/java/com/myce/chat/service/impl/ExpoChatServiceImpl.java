package com.myce.chat.service.impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.security.util.JwtUtil;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.service.ExpoChatService;
import com.myce.common.dto.PageResponse;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 박람회 관리자 채팅 서비스 구현체
 * 기존 Service + ServiceImpl 패턴 준수
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpoChatServiceImpl implements ExpoChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<ChatRoomListResponse> getChatRoomsForAdmin(Long expoId, CustomUserDetails userDetails) {
        // 권한 검증
        validateAdminPermission(expoId, userDetails);
        
        // 해당 박람회의 채팅방 목록 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(expoId);
        
        
        // DTO로 변환
        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfos = chatRooms.stream()
                .map(this::mapToChatRoomListResponse)
                .collect(Collectors.toList());
                
        return List.of(ChatRoomListResponse.builder()
                .chatRooms(chatRoomInfos)
                .totalCount(chatRoomInfos.size())
                .build());
    }

    @Override
    public PageResponse<MessageResponse> getMessages(Long expoId, String roomCode, Pageable pageable, CustomUserDetails userDetails) {
        // 권한 검증
        validateAdminPermission(expoId, userDetails);
        
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // 메시지 조회
        Page<ChatMessage> messages = chatMessageRepository.findByRoomCodeOrderBySentAtDesc(roomCode, pageable);
        
        
        // DTO로 변환  
        List<MessageResponse> messageResponses = messages.getContent().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                messageResponses,
                messages.getNumber(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void markAsRead(Long expoId, String roomCode, String lastReadMessageId, CustomUserDetails userDetails) {
        // 권한 검증
        validateAdminPermission(expoId, userDetails);
        
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // 마지막 메시지 ID를 가져와서 읽음 처리 (가장 최근 메시지까지 읽음 처리)
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
        if (!recentMessages.isEmpty()) {
            String latestMessageId = recentMessages.get(0).getId();
            
            // readStatusJson 업데이트
            String currentReadStatus = chatRoom.getReadStatusJson();
            String updatedReadStatus = updateReadStatusForAdmin(currentReadStatus, latestMessageId);
            chatRoom.updateReadStatus(updatedReadStatus);
        }
        
        // 관리자 활동 시간 업데이트 (담당자가 있을 경우)
        if (chatRoom.hasAssignedAdmin()) {
            chatRoom.updateAdminActivity();
        }
        
        chatRoomRepository.save(chatRoom);
        
        // WebSocket을 통해 상대방(사용자)에게 읽음 상태 변경 알림
        try {
            Map<String, Object> readStatusPayload = Map.of(
                "roomCode", roomCode,
                "readerType", "ADMIN",
                "unreadCount", 0
            );
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "read_status_update",
                "payload", readStatusPayload
            );
            
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomCode,
                broadcastMessage
            );
            
        } catch (Exception e) {
            log.warn("읽음 상태 WebSocket 알림 전송 실패 - roomCode: {}, error: {}", roomCode, e.getMessage());
        }
        
    }
    
    /**
     * 관리자 읽음 상태 업데이트
     */
    private String updateReadStatusForAdmin(String currentReadStatus, String lastReadMessageId) {
        if (currentReadStatus == null || currentReadStatus.isEmpty() || currentReadStatus.equals("{}")) {
            return "{\"ADMIN\":\"" + lastReadMessageId + "\"}";
        }
        
        // 기존 ADMIN 정보가 있으면 업데이트, 없으면 추가
        if (currentReadStatus.contains("\"ADMIN\"")) {
            return currentReadStatus.replaceAll("\"ADMIN\":\"[^\"]*\"", "\"ADMIN\":\"" + lastReadMessageId + "\"");
        } else {
            return currentReadStatus.substring(0, currentReadStatus.length() - 1) + 
                   ",\"ADMIN\":\"" + lastReadMessageId + "\"}";
        }
    }

    @Override
    public Long getUnreadCount(Long expoId, String roomCode, CustomUserDetails userDetails) {
        // 권한 검증  
        validateAdminPermission(expoId, userDetails);
        
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        try {
            // 관리자 입장에서는 사용자가 보낸 메시지만 안읽은 메시지로 계산
            // TODO: 실제로는 마지막 읽은 메시지 ID 기준으로 계산해야 함
            // 현재는 간단히 사용자 메시지 개수로 계산
            Long unreadCount = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "USER");
            
            return unreadCount;
            
        } catch (Exception e) {
            log.error("안읽은 메시지 수 조회 실패 - roomCode: {}", roomCode, e);
            return 0L; // 에러 시 0 반환
        }
    }

    /**
     * 관리자 권한 검증
     */
    private void validateAdminPermission(Long expoId, CustomUserDetails userDetails) {
        String loginType = userDetails.getLoginType().name();
        Long memberId = userDetails.getMemberId();
        
        
        if ("ADMIN_CODE".equals(loginType)) {
            // AdminCode 권한 체크
            AdminCode adminCode = adminCodeRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("AdminCode를 찾을 수 없습니다. memberId(adminCodeId): {}", memberId);
                        return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
                    });
            
            
            // 박람회 일치 확인
            if (!adminCode.getExpoId().equals(expoId)) {
                log.error("박람회 ID 불일치 - adminCode.expoId: {}, 요청 expoId: {}", 
                        adminCode.getExpoId(), expoId);
                throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
            }
            
            // 채팅 권한 확인
            if (adminCode.getAdminPermission() != null && 
                !adminCode.getAdminPermission().getIsInquiryView()) {
                log.error("문의 보기 권한 없음 - isInquiryView: {}", 
                        adminCode.getAdminPermission().getIsInquiryView());
                throw new AccessDeniedException("문의 보기 권한이 없습니다");
            }
            
            
        } else if ("MEMBER".equals(loginType)) {
            // Super Admin 권한 체크
            Member superAdmin = memberRepository.findByExpoId(expoId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
            
            if (!superAdmin.getId().equals(memberId)) {
                throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
            }
            
        } else {
            throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }

    /**
     * ChatRoom -> ChatRoomListResponse 매핑
     */
    private ChatRoomListResponse.ChatRoomInfo mapToChatRoomListResponse(ChatRoom chatRoom) {
        return ChatRoomListResponse.ChatRoomInfo.builder()
                .id(chatRoom.getId())
                .roomCode(chatRoom.getRoomCode())
                .expoId(chatRoom.getExpoId())
                .expoTitle(chatRoom.getExpoTitle())
                .otherMemberId(chatRoom.getMemberId())
                .otherMemberName(chatRoom.getMemberName())
                .otherMemberRole("USER") // 관리자 입장에서는 상대방이 사용자
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(calculateUnreadCount(chatRoom.getRoomCode()))
                .isActive(chatRoom.getIsActive())
                .currentAdminCode(chatRoom.getCurrentAdminCode())
                .adminDisplayName(chatRoom.getAdminDisplayName())
                .build();
    }

    /**
     * ChatMessage -> MessageResponse 매핑
     */
    private MessageResponse mapToMessageResponse(ChatMessage message) {
        // 읽음 상태 계산 (현재는 임시로 0으로 설정, 실제 로직은 별도 구현 필요)
        Integer unreadCount = calculateMessageUnreadCount(message);
        
        return MessageResponse.builder()
                .roomId(message.getRoomCode())
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .unreadCount(unreadCount)
                .build();
    }
    
    /**
     * 개별 메시지의 읽지 않은 수 계산 (카카오톡 스타일)
     */
    private Integer calculateMessageUnreadCount(ChatMessage message) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(message.getRoomCode()).orElse(null);
            if (chatRoom == null) {
                return 1; // 채팅방이 없으면 안읽음으로 표시
            }
            
            String readStatusJson = chatRoom.getReadStatusJson();
            
            // 메시지 발송자에 따라 상대방의 읽음 상태 확인
            if ("ADMIN".equals(message.getSenderType())) {
                // 관리자가 보낸 메시지 -> 사용자가 읽었는지 확인
                String userLastReadId = extractLastReadMessageId(readStatusJson, "USER");
                if (userLastReadId == null || message.getId().compareTo(userLastReadId) > 0) {
                    return 1; // 사용자가 안 읽음
                }
            } else {
                // 사용자가 보낸 메시지 -> 관리자가 읽었는지 확인
                String adminLastReadId = extractLastReadMessageId(readStatusJson, "ADMIN");
                if (adminLastReadId == null || message.getId().compareTo(adminLastReadId) > 0) {
                    return 1; // 관리자가 안 읽음
                }
            }
            
            return 0; // 읽음
        } catch (Exception e) {
            log.warn("메시지 읽음 상태 계산 실패 - messageId: {}", message.getId());
            return 1; // 에러시 안읽음으로 표시
        }
    }
    
    /**
     * 안읽은 메시지 개수 계산 (관리자가 마지막으로 읽은 메시지 이후의 사용자 메시지만)
     */
    private Integer calculateUnreadCount(String roomCode) {
        try {
            // ChatRoom에서 관리자의 마지막 읽은 메시지 ID 조회
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
            if (chatRoom == null) {
                return 0;
            }
            
            // readStatusJson에서 ADMIN의 마지막 읽은 메시지 ID 추출
            String lastReadMessageId = extractLastReadMessageId(chatRoom.getReadStatusJson(), "ADMIN");
            
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
            log.warn("안읽은 메시지 개수 계산 실패 - roomCode: {}", roomCode);
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
    
    @Override
    public Map<String, Object> getAllUnreadCountsForUser(CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getMemberId();
            
            // 해당 사용자의 모든 활성 채팅방 조회
            List<ChatRoom> userChatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(userId);
            
            if (userChatRooms.isEmpty()) {
                return Map.of(
                    "totalUnreadCount", 0,
                    "unreadCounts", List.of()
                );
            }
            
            // 각 채팅방별 읽지 않은 메시지 수 계산
            List<Map<String, Object>> unreadCounts = userChatRooms.stream()
                    .map(room -> {
                        Integer count = calculateUnreadCountForUser(room.getRoomCode(), userId);
                        Map<String, Object> roomUnread = new HashMap<>();
                        roomUnread.put("roomCode", room.getRoomCode());
                        roomUnread.put("unreadCount", count);
                        return roomUnread;
                    })
                    .collect(Collectors.toList());
            
            // 전체 읽지 않은 메시지 수 계산
            int totalUnreadCount = unreadCounts.stream()
                    .mapToInt(map -> (Integer) map.get("unreadCount"))
                    .sum();
            
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCounts", unreadCounts);
            result.put("totalUnreadCount", totalUnreadCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("사용자 전체 읽지 않은 메시지 수 조회 실패 - userId: {}", userDetails.getMemberId(), e);
            // 에러 시 빈 결과 반환
            return Map.of(
                "totalUnreadCount", 0,
                "unreadCounts", List.of()
            );
        }
    }
    
    /**
     * 사용자 관점에서 안읽은 메시지 개수 계산 (관리자 메시지만 계산)
     */
    private Integer calculateUnreadCountForUser(String roomCode, Long userId) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
            if (chatRoom == null) {
                return 0;
            }
            
            // readStatusJson에서 USER의 마지막 읽은 메시지 ID 추출
            String lastReadMessageId = extractLastReadMessageId(chatRoom.getReadStatusJson(), "USER");
            
            if (lastReadMessageId == null || lastReadMessageId.isEmpty()) {
                // 사용자가 아직 아무것도 읽지 않았다면 전체 ADMIN 메시지 개수
                Long count = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "ADMIN");
                return count.intValue();
            } else {
                // 마지막 읽은 메시지 ID 이후의 ADMIN 메시지 개수
                Long count = chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                    roomCode, "ADMIN", lastReadMessageId);
                return count.intValue();
            }
        } catch (Exception e) {
            log.warn("사용자 안읽은 메시지 개수 계산 실패 - roomCode: {}, userId: {}", roomCode, userId);
            return 0;
        }
    }
}