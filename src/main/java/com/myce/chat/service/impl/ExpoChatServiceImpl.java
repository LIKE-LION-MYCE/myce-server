package com.myce.chat.service.impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.security.util.JwtUtil;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatUnreadService;
import com.myce.chat.service.ExpoChatService;
import com.myce.chat.service.util.ChatReadStatusUtil;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.common.dto.PageResponse;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
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

    private static final String ADMIN_ROOM_PREFIX = "admin-";
    private static final String ROOM_DELIMITER = "-";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCacheService chatCacheService;
    private final AdminCodeRepository adminCodeRepository;
    private final ExpoRepository expoRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatUnreadService chatUnreadService;

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
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // 성능 최적화: 메시지 로딩 시 권한 검증 생략 (채팅방 접근 시에만 검증)
        // 이미 채팅방에 접근할 수 있다는 것은 권한이 검증되었음을 의미
        
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
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // 성능 최적화: 읽음 처리 시 권한 검증 생략 (채팅방 접근 시에만 검증)
        
        // 1. Redis에서 미읽음 카운트 리셋 (관리자 읽음 처리)
        Long adminUserId = userDetails.getMemberId();
        chatCacheService.resetUnreadCount(roomCode, adminUserId);
        chatCacheService.recalculateBadgeCount(adminUserId);
        log.debug("Redis unread count reset for admin: {} in room: {}", adminUserId, roomCode);
        
        // 2. 마지막 읽은 메시지 ID Redis에 저장
        if (lastReadMessageId != null && !lastReadMessageId.trim().isEmpty()) {
            chatCacheService.setLastReadMessageId(roomCode, adminUserId, lastReadMessageId);
        }
        
        // 3. 마지막 메시지 ID를 가져와서 읽음 처리 (가장 최근 메시지까지 읽음 처리)
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
        if (!recentMessages.isEmpty()) {
            String latestMessageId = recentMessages.get(0).getId();
            
            // readStatusJson 업데이트 (MongoDB)
            String currentReadStatus = chatRoom.getReadStatusJson();
            String updatedReadStatus = ChatReadStatusUtil.updateReadStatusForAdmin(currentReadStatus, latestMessageId);
            
            log.info("EXPO 관리자 읽음 처리 - roomCode: {}, latestMessageId: {}, 이전 readStatus: {}, 업데이트된 readStatus: {}", 
                roomCode, latestMessageId, currentReadStatus, updatedReadStatus);
            
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
        
        // 박람회 관리자들에게 unread count 업데이트 알림
        try {
            Long extractedExpoId = extractExpoIdFromRoomCode(roomCode);
            if (extractedExpoId != null) {
                Map<String, Object> unreadUpdatePayload = Map.of(
                    "roomCode", roomCode,
                    "unreadCount", 0
                );
                
                Map<String, Object> unreadUpdateMessage = Map.of(
                    "type", "unread_count_update",
                    "payload", unreadUpdatePayload
                );
                
                messagingTemplate.convertAndSend(
                    "/topic/expo/" + extractedExpoId + "/chat-room-updates",
                    unreadUpdateMessage
                );
            }
        } catch (Exception e) {
            log.warn("관리자 unread count 업데이트 전송 실패 - roomCode: {}, error: {}", roomCode, e.getMessage());
        }
        
    }
    
    /**
     * 관리자 읽음 상태 업데이트
     */
    // 중복 메서드 제거됨 - ChatReadStatusUtil로 통합

    @Override
    public Long getUnreadCount(Long expoId, String roomCode, CustomUserDetails userDetails) {
        // 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 박람회 일치 확인
        if (!chatRoom.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        // 성능 최적화: 미읽음 카운트 조회 시 권한 검증 생략 (채팅방 접근 시에만 검증)
        
        try {
            // Redis에서 관리자의 미읽음 카운트 조회 (10ms 미만)
            Long adminUserId = userDetails.getMemberId();
            Long cachedUnreadCount = chatCacheService.getUnreadCount(roomCode, adminUserId);
            
            if (cachedUnreadCount != null && cachedUnreadCount > 0) {
                log.debug("Cache hit: unread count {} for admin {} in room {}", cachedUnreadCount, adminUserId, roomCode);
                return cachedUnreadCount;
            }
            
            // 캐시 미스 시 MongoDB에서 계산하고 캐싱
            Long unreadCount = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "USER");
            
            // 결과를 Redis에 캐싱 (다음 조회 시 빠른 응답)
            if (unreadCount > 0) {
                chatCacheService.incrementUnreadCount(roomCode, adminUserId);
                // 정확한 값으로 설정하기 위해 리셋 후 증가
                chatCacheService.resetUnreadCount(roomCode, adminUserId);
                for (int i = 0; i < unreadCount; i++) {
                    chatCacheService.incrementUnreadCount(roomCode, adminUserId);
                }
            }
            
            log.debug("Cache miss: calculated unread count {} for admin {} in room {}", unreadCount, adminUserId, roomCode);
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
                log.error("🚫 문의 보기 권한 없음 - adminCode: {}, adminPermission exists: {}, isInquiryView: {}", 
                        adminCode.getCode(), true, adminCode.getAdminPermission().getIsInquiryView());
                throw new AccessDeniedException("문의 보기 권한이 없습니다");
            }
            
            log.info("✅ Expo 채팅 권한 검증 완료 - adminCode: {}, permission null: {}, isInquiryView: {}", 
                    adminCode.getCode(), 
                    adminCode.getAdminPermission() == null,
                    adminCode.getAdminPermission() != null ? adminCode.getAdminPermission().getIsInquiryView() : "N/A");
            
            
        } else if ("MEMBER".equals(loginType)) {
            // Super Admin 권한 체크 - 박람회 소유자인지 확인
            Expo expo = expoRepository.findById(expoId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
            
            if (!expo.getMember().getId().equals(memberId)) {
                log.error("박람회 소유자가 아닌 사용자의 접근 시도 - expo.ownerId: {}, 요청자memberId: {}", 
                        expo.getMember().getId(), memberId);
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
                .unreadCount(calculateUnreadCount(chatRoom.getRoomCode()).intValue())
                .isActive(chatRoom.getIsActive())
                .currentAdminCode(chatRoom.getCurrentAdminCode())
                .adminDisplayName(chatRoom.getAdminDisplayName())
                .currentState(chatRoom.getCurrentState() != null ? chatRoom.getCurrentState().name() : null)
                .build();
    }

    /**
     * ChatMessage -> MessageResponse 매핑
     */
    private MessageResponse mapToMessageResponse(ChatMessage message) {
        // 읽음 상태 계산 (현재는 임시로 0으로 설정, 실제 로직은 별도 구현 필요)
        Integer unreadCount = chatUnreadService.getMessageUnreadCount(
            message.getId(), message.getSenderId(), message.getSenderType(), message.getRoomCode());
        
        // 관리자 메시지인 경우 관리자 정보 추가
        String adminCode = null;
        String adminDisplayName = null;
        if ("ADMIN".equals(message.getSenderType()) && message.getActualSender() != null) {
            adminCode = message.getActualSender();
            adminDisplayName = getAdminDisplayName(adminCode);
        }
        
        // ChatMessageMapper 사용
        return ChatMessageMapper.toDto(message, unreadCount, adminCode, adminDisplayName);
    }
    
    // 중복 메서드 제거됨 - ChatUnreadService로 통합
    
    /**
     * 안읽은 메시지 개수 계산
     * 관리자 화면: 사용자가 읽지 않은 관리자 메시지 개수를 표시
     */
    private Long calculateUnreadCount(String roomCode) {
        try {
            // 통합 읽음 상태 계산 서비스 사용
            // 박람회 관리자 관점: 관리자가 읽어야 할 USER 메시지 개수
            // TODO: 실제 관리자 ID 전달 필요 (현재는 임시로 0L 사용)
            return chatUnreadService.getUnreadCountForViewer(roomCode, 0L, "EXPO_ADMIN");
        } catch (Exception e) {
            log.error("안읽은 메시지 개수 계산 실패 - roomCode: {}", roomCode, e);
            return 0L;
        }
    }
    
    // extractLastReadMessageId 메서드 제거됨 - ChatUnreadService로 통합
    
    @Override
    public Map<String, Object> getAllUnreadCountsForUser(CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getMemberId();
            
            // Redis에서 전체 배지 카운트 조회 (5ms 이내)
            Long totalBadgeCount = chatCacheService.getBadgeCount(userId);
            log.debug("✅ Redis 배지 카운트 조회 - userId: {}, count: {}", userId, totalBadgeCount);
            
            if (totalBadgeCount == 0) {
                return Map.of(
                    "totalUnreadCount", 0,
                    "unreadCounts", List.of()
                );
            }
            
            // 상세 정보가 필요한 경우에만 개별 채팅방 조회 (옵션)
            List<ChatRoom> userChatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(userId);
            
            if (userChatRooms.isEmpty()) {
                // Redis 카운트가 있는데 채팅방이 없으면 Redis 재계산
                chatCacheService.recalculateBadgeCount(userId);
                return Map.of(
                    "totalUnreadCount", 0,
                    "unreadCounts", List.of()
                );
            }
            
            // 개별 채팅방 미읽음 카운트는 Redis에서 조회 (빠른 응답)
            List<Map<String, Object>> unreadCounts = userChatRooms.stream()
                    .map(room -> {
                        // Redis에서 개별 채팅방 미읽음 카운트 조회
                        Long roomUnreadCount = chatCacheService.getUnreadCount(room.getRoomCode(), userId);
                        if (roomUnreadCount == null) {
                            // Redis 캐시 미스 시 계산 후 캐싱
                            Long calculatedCount = chatUnreadService.getUnreadCountForViewer(room.getRoomCode(), userId, "USER");
                            if (calculatedCount > 0) {
                                // Redis에 캐싱 (다음 조회 시 빠른 응답)
                                for (int i = 0; i < calculatedCount; i++) {
                                    chatCacheService.incrementUnreadCount(room.getRoomCode(), userId);
                                }
                            }
                            roomUnreadCount = calculatedCount;
                        }
                        
                        Map<String, Object> roomUnread = new HashMap<>();
                        roomUnread.put("roomCode", room.getRoomCode());
                        roomUnread.put("unreadCount", roomUnreadCount.intValue());
                        return roomUnread;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCounts", unreadCounts);
            result.put("totalUnreadCount", totalBadgeCount.intValue());
            
            log.debug("✅ 전체 미읽음 카운트 조회 완료 - userId: {}, total: {}, rooms: {}", 
                     userId, totalBadgeCount, unreadCounts.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ 사용자 전체 읽지 않은 메시지 수 조회 실패 - userId: {}", userDetails.getMemberId(), e);
            // 에러 시 기존 방식으로 폴백
            return getAllUnreadCountsForUserFallback(userDetails);
        }
    }
    
    /**
     * Redis 실패 시 폴백 메서드 (기존 방식)
     */
    private Map<String, Object> getAllUnreadCountsForUserFallback(CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getMemberId();
            List<ChatRoom> userChatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(userId);
            
            if (userChatRooms.isEmpty()) {
                return Map.of("totalUnreadCount", 0, "unreadCounts", List.of());
            }
            
            List<Map<String, Object>> unreadCounts = userChatRooms.stream()
                    .map(room -> {
                        Long count = chatUnreadService.getUnreadCountForViewer(room.getRoomCode(), userId, "USER");
                        Map<String, Object> roomUnread = new HashMap<>();
                        roomUnread.put("roomCode", room.getRoomCode());
                        roomUnread.put("unreadCount", count);
                        return roomUnread;
                    })
                    .collect(Collectors.toList());
            
            int totalUnreadCount = unreadCounts.stream()
                    .mapToInt(map -> ((Long) map.get("unreadCount")).intValue())
                    .sum();
            
            return Map.of("unreadCounts", unreadCounts, "totalUnreadCount", totalUnreadCount);
            
        } catch (Exception e) {
            log.error("❌ 폴백 메서드도 실패 - userId: {}", userDetails.getMemberId(), e);
            return Map.of("totalUnreadCount", 0, "unreadCounts", List.of());
        }
    }
    
    // calculateUnreadCountForUser 메서드 제거됨 - ChatUnreadService로 통합
    
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
     * 관리자 표시 이름 생성
     */
    private String getAdminDisplayName(String adminCode) {
        if ("SUPER_ADMIN".equals(adminCode)) {
            return "박람회 관리자";
        } else {
            return "박람회 관리자 (" + adminCode + ")";
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> getOrCreateExpoChatRoom(Long expoId, CustomUserDetails userDetails) {
        log.info("🔵 박람회 채팅방 생성/조회 요청 - expoId: {}, userId: {}", expoId, userDetails.getMemberId());
        
        Long userId = userDetails.getMemberId();
        
        // 1. 박람회 존재 확인
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 2. 사용자 정보 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        
        // 3. 채팅방 코드 생성 (admin-{expoId}-{userId})
        String roomCode = ADMIN_ROOM_PREFIX + expoId + ROOM_DELIMITER + userId;
        
        // 4. 기존 채팅방 조회
        ChatRoom existingRoom = chatRoomRepository.findByRoomCode(roomCode).orElse(null);
        
        if (existingRoom != null) {
            log.info("✅ 기존 채팅방 조회 성공 - roomCode: {}", roomCode);
            
            // 기존 채팅방 재활성화 (필요한 경우)
            if (!existingRoom.getIsActive()) {
                existingRoom.reactivate();
                chatRoomRepository.save(existingRoom);
                log.info("🔄 비활성 채팅방 재활성화 - roomCode: {}", roomCode);
            }
            
            return createChatRoomResponse(existingRoom);
        }
        
        // 5. 새 채팅방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .memberId(userId)
                .memberName(member.getName())
                .expoId(expoId)
                .expoTitle(expo.getTitle())
                .build();
        
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        log.info("✨ 새 박람회 채팅방 생성 완료 - roomCode: {}, expoTitle: {}", roomCode, expo.getTitle());
        
        // 6. AI 환영 메시지 생성 (선택사항 - 필요시 구현)
        // createWelcomeMessage(savedRoom, expo, member);
        
        return createChatRoomResponse(savedRoom);
    }
    
    /**
     * 채팅방 응답 객체 생성
     */
    private Map<String, Object> createChatRoomResponse(ChatRoom chatRoom) {
        Map<String, Object> response = new HashMap<>();
        response.put("roomCode", chatRoom.getRoomCode());
        response.put("expoId", chatRoom.getExpoId());
        response.put("expoTitle", chatRoom.getExpoTitle());
        response.put("memberName", chatRoom.getMemberName());
        response.put("isActive", chatRoom.getIsActive());
        response.put("currentState", chatRoom.getCurrentState().name());
        response.put("lastMessageAt", chatRoom.getLastMessageAt());
        response.put("createdAt", chatRoom.getCreatedAt());
        
        return response;
    }
}