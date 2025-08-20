package com.myce.chat.service.impl;

import com.myce.auth.security.util.JwtUtil;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.ChatWebSocketService;
import com.myce.chat.service.mapper.ChatMessageMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.Role;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 채팅 WebSocket 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWebSocketServiceImpl implements ChatWebSocketService {

    private static final String ADMIN_ROOM_PREFIX = "admin-";
    private static final String ROOM_DELIMITER = "-";
    private static final String PLATFORM_ROOM_PREFIX = "platform-";

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final AdminCodeRepository adminCodeRepository;
    private final ExpoRepository expoRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageService chatMessageService;
    private final ChatCacheService chatCacheService;

    @Override
    public Long authenticateUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
            }

            Long userId = jwtUtil.getMemberIdFromToken(token);
            String loginType = jwtUtil.getLoginTypeFromToken(token);
            
            if ("ADMIN_CODE".equals(loginType)) {
                adminCodeRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            } else {
                memberRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            }
            
            return userId;
            
        } catch (Exception e) {
            log.error("WebSocket JWT 인증 실패: {}", e.getMessage());
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }
    }

    @Override
    @Transactional
    public void joinRoom(Long userId, String roomId, String token) {
        if (!isValidRoomIdFormat(roomId)) {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        
        // 플랫폼 방 처리
        if (roomId.startsWith(PLATFORM_ROOM_PREFIX)) {
            String[] parts = roomId.split(ROOM_DELIMITER);
            Long roomMemberId = Long.parseLong(parts[1]);
            
            // 권한 확인: 본인의 플랫폼 방이거나 플랫폼 관리자
            String loginType = jwtUtil.getLoginTypeFromToken(token);
            Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            
            boolean isOwner = userId.equals(roomMemberId);
            boolean isPlatformAdmin = Role.PLATFORM_ADMIN.name().equals(user.getRole().name());
            
            if (!isOwner && !isPlatformAdmin) {
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
            
            ensurePlatformChatRoomExists(roomId, roomMemberId);
            return;
        }
        
        // 기존 박람회 방 처리
        String[] parts = roomId.split(ROOM_DELIMITER);
        Long expoId = Long.parseLong(parts[1]);
        Long participantId = Long.parseLong(parts[2]);
        
        String loginType = jwtUtil.getLoginTypeFromToken(token);
        
        if ("ADMIN_CODE".equals(loginType)) {
            AdminCode adminCode = adminCodeRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            
            if (!adminCode.getExpoId().equals(expoId)) {
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
            
        } else {
            Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            
            if (Role.EXPO_ADMIN.name().equals(user.getRole().name())) {
                boolean isExpoOwner = expoRepository.existsByIdAndMemberId(expoId, userId);
                boolean isParticipant = userId.equals(participantId);
                if (!isExpoOwner && !isParticipant) {
                    throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
            } else if (Role.USER.name().equals(user.getRole().name())) {
                if (!userId.equals(participantId)) {
                    throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
            } else {
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        }
        
        ensureChatRoomExists(roomId, expoId, participantId);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long userId, String roomId, String content, String token) {
        log.error("🚨 SENDMESSAGE 시작 - userId: {}, roomId: {}, content: '{}'", userId, roomId, content);
        String senderRole;
        String senderName;
        
        // Handle platform rooms (format: platform-{userId})
        if (roomId.startsWith("platform-")) {
            Member sender = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            
            if (Role.PLATFORM_ADMIN.name().equals(sender.getRole().name())) {
                senderRole = "PLATFORM_ADMIN";
                senderName = "플랫폼 관리자";
            } else {
                senderRole = "USER";
                senderName = sender.getName();
            }
            
            log.debug("🎭 Platform 메시지 발송자 정보: userId={}, senderRole={}, senderName={}, memberRole={}", 
                userId, senderRole, senderName, sender.getRole().name());
        } else {
            // Handle expo rooms (format: admin-{expoId}-{userId})
            String[] parts = roomId.split(ROOM_DELIMITER);
            Long expoId = Long.parseLong(parts[1]);
            
            // Use token to determine login type - same as joinRoom logic
            String loginType = jwtUtil.getLoginTypeFromToken(token);
            
            if ("ADMIN_CODE".equals(loginType)) {
                // This is an AdminCode user
                AdminCode adminCode = adminCodeRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
                
                if (!adminCode.getExpoId().equals(expoId)) {
                    throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
                
                senderRole = "ADMIN";
                senderName = "박람회 관리자 (상담원)";
            } else {
                // This is a regular Member user - apply platform chat logic
                Member sender = memberRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
                
                if (Role.EXPO_ADMIN.name().equals(sender.getRole().name())) {
                    // Check if they're the expo owner
                    boolean isExpoOwner = expoRepository.existsByIdAndMemberId(expoId, userId);
                    if (isExpoOwner) {
                        senderRole = "ADMIN";
                        senderName = "박람회 관리자";
                    } else {
                        // EXPO_ADMIN이지만 박람회 소유자가 아닌 경우 일반 USER로 취급
                        senderRole = "USER";
                        senderName = sender.getName();
                    }
                } else if (Role.USER.name().equals(sender.getRole().name())) {
                    // Regular user - correctly identify as USER
                    senderRole = "USER";
                    senderName = sender.getName();
                } else {
                    throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
            }
            
            log.debug("🎭 Expo 메시지 발송자 정보: userId={}, senderRole={}, senderName={}, loginType={}, expoId={}", 
                userId, senderRole, senderName, loginType, expoId);
        }
        
        ChatMessage chatMessage = chatMessageService.createMessage(
            roomId, senderRole, userId, senderName, content
        );
        
        // 1. Redis에 즉시 메시지 추가 (비동기)
        chatCacheService.addMessageToCache(roomId, chatMessage);
        
        // 2. 미읽음 카운트 증가 (수신자 찾기)
        Long receiverId = getReceiverId(roomId, userId, senderRole);
        if (receiverId != null) {
            chatCacheService.incrementUnreadCount(roomId, receiverId);
            chatCacheService.incrementBadgeCount(receiverId);
            log.debug("Updated unread count for receiver: {} in room: {}", receiverId, roomId);
        }
        
        // 3. 사용자 활성 채팅방에 추가
        chatCacheService.addUserActiveRoom(userId, roomId);
        if (receiverId != null) {
            chatCacheService.addUserActiveRoom(receiverId, roomId);
        }
        
        // 4. MongoDB 저장 및 채팅방 업데이트 (동기 - 임시)
        try {
            log.warn("🔧 Starting MongoDB save - messageId: {}, roomId: {}", chatMessage.getId(), roomId);
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            log.warn("🔧 ChatMessage saved to MongoDB - messageId: {}, roomId: {}", savedMessage.getId(), roomId);
            updateChatRoomLastMessage(roomId, savedMessage.getId(), content);
            log.warn("✅ MongoDB 저장 성공 - messageId: {}, roomId: {}", savedMessage.getId(), roomId);
        } catch (Exception e) {
            log.error("❌ MongoDB 저장 실패 - roomId: {}, messageId: {}, error: {}", 
                     roomId, chatMessage.getId(), e.getMessage(), e);
        }
        
        return ChatMessageMapper.toSendResponse(chatMessage, roomId);
    }

    /**
     * 수신자 ID 찾기
     * 채팅방 타입에 따라 수신자 결정
     */
    private Long getReceiverId(String roomId, Long senderId, String senderRole) {
        try {
            if (roomId.startsWith(PLATFORM_ROOM_PREFIX)) {
                // 플랫폼 채팅: platform-{userId}
                String[] parts = roomId.split(ROOM_DELIMITER);
                Long roomUserId = Long.parseLong(parts[1]);
                
                if ("PLATFORM_ADMIN".equals(senderRole)) {
                    // 플랫폼 관리자가 보낸 경우 → 사용자가 수신자
                    return roomUserId;
                } else {
                    // 사용자가 보낸 경우 → 현재 활성 플랫폼 관리자가 수신자
                    try {
                        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
                        if (chatRoom != null && chatRoom.getCurrentState() == ChatRoom.ChatRoomState.ADMIN_ACTIVE) {
                            // ADMIN_ACTIVE 상태일 때 현재 담당 관리자의 ID 반환
                            String currentAdminCode = chatRoom.getCurrentAdminCode();
                            if ("PLATFORM_ADMIN".equals(currentAdminCode)) {
                                // 플랫폼 관리자 중 첫 번째 활성 관리자 찾기 (임시로 null 반환)
                                // TODO: 실제 활성 플랫폼 관리자 ID를 찾는 로직 구현 필요
                                return null;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to find active platform admin for room: {}", roomId, e);
                    }
                    return null; // AI 상태이거나 관리자를 찾을 수 없는 경우
                }
            } else if (roomId.startsWith(ADMIN_ROOM_PREFIX)) {
                // 박람회 채팅: admin-{expoId}-{userId}
                String[] parts = roomId.split(ROOM_DELIMITER);
                Long expoId = Long.parseLong(parts[1]);
                Long participantId = Long.parseLong(parts[2]);
                
                if ("ADMIN".equals(senderRole)) {
                    // 관리자가 보낸 경우 → 참가자가 수신자
                    return participantId;
                } else {
                    // 사용자가 보낸 경우 → 현재 배정된 관리자가 수신자
                    // ChatRoom에서 현재 배정된 관리자 정보 확인
                    try {
                        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomId).orElse(null);
                        if (chatRoom != null && chatRoom.hasAssignedAdmin()) {
                            // 관리자가 배정된 경우, 해당 관리자의 ID를 찾아서 반환
                            String adminCode = chatRoom.getCurrentAdminCode();
                            if ("SUPER_ADMIN".equals(adminCode)) {
                                // Super Admin의 경우 박람회 소유자 ID 반환
                                return expoRepository.findById(expoId)
                                    .map(expo -> expo.getMember().getId())
                                    .orElse(null);
                            } else {
                                // AdminCode의 경우 해당 AdminCode ID 반환
                                return adminCodeRepository.findByCode(adminCode)
                                    .map(admin -> admin.getId())
                                    .orElse(null);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to find assigned admin for room: {}", roomId, e);
                    }
                    return null;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error finding receiver ID for room: {}, sender: {}", roomId, senderId, e);
            return null;
        }
    }

    /**
     * roomId 형식 검증
     */
    private boolean isValidRoomIdFormat(String roomId) {
        if (roomId == null) {
            return false;
        }
        
        // 플랫폼 방 형식: platform-{memberId}
        if (roomId.startsWith(PLATFORM_ROOM_PREFIX)) {
            String[] parts = roomId.split(ROOM_DELIMITER);
            if (parts.length != 2) return false;
            try {
                Long.parseLong(parts[1]); // memberId
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // 기존 박람회 방 형식: admin-{expoId}-{memberId} (원래 로직 유지)
        if (!roomId.startsWith(ADMIN_ROOM_PREFIX)) {
            return false;
        }
        
        String[] parts = roomId.split(ROOM_DELIMITER);
        if (parts.length != 3) {
            return false;
        }
        
        try {
            Long.parseLong(parts[1]); // expoId
            Long.parseLong(parts[2]); // userId
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 채팅방 존재 확인 및 생성
     */
    private void ensureChatRoomExists(String roomId, Long expoId, Long participantId) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomCode(roomId);
        
        if (existingRoom.isEmpty()) {
            ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomId)
                .expoId(expoId)
                .memberId(participantId)
                .build();
                
            chatRoomRepository.save(newRoom);
        }
    }

    /**
     * 플랫폼 채팅방 존재 확인 및 생성
     */
    private void ensurePlatformChatRoomExists(String roomCode, Long memberId) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomCode(roomCode);
        
        if (existingRoom.isEmpty()) {
            // Fetch actual user name from database
            String memberName = "플랫폼 사용자"; // Default fallback
            try {
                Optional<Member> memberOpt = memberRepository.findById(memberId);
                if (memberOpt.isPresent()) {
                    memberName = memberOpt.get().getName();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch member name for platform room creation: {}", e.getMessage());
            }
            
            ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomCode)
                .expoId(null)  // 플랫폼 방은 expoId 없음
                .memberId(memberId)
                .memberName(memberName)  // Use actual user name
                .expoTitle("플랫폼 상담")    // 플랫폼 방 표시용
                .build();
                
            chatRoomRepository.save(newRoom);
            log.info("플랫폼 채팅방 생성 완료 - roomCode: {}, memberId: {}, memberName: {}", roomCode, memberId, memberName);
        }
    }

    /**
     * 채팅방 마지막 메시지 업데이트
     */
    private void updateChatRoomLastMessage(String roomId, String messageId, String content) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomId);
        
        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatRoom.updateLastMessageInfo(messageId, content);
            chatRoomRepository.save(chatRoom);
        }
    }

    /**
     * 관리자 담당자 배정 로직
     */
    @Override
    public void assignAdminIfNeeded(ChatRoom chatRoom, String adminCode) {
        log.info("🔧 assignAdminIfNeeded called - room: {}, adminCode: {}, currentState: {}, hasAssignedAdmin: {}", 
                chatRoom.getRoomCode(), adminCode, chatRoom.getCurrentState(), chatRoom.hasAssignedAdmin());
        
        try {
            boolean needsUpdate = false;
            
            if (!chatRoom.hasAssignedAdmin()) {
                log.info("🔧 No admin assigned, attempting to assign: {}", adminCode);
                try {
                    // Atomic assignment with collision protection
                    boolean assigned = chatRoom.assignAdmin(adminCode);
                    log.info("🔧 assignAdmin result: {}", assigned);
                    if (assigned) {
                        String displayName = getAdminDisplayName(adminCode);
                        log.info("🔧 Generated displayName: {}", displayName);
                        chatRoom.setAdminDisplayName(displayName);
                        needsUpdate = true;
                        log.info("✅ Admin assigned successfully: {} to room {} - NEW STATE: {}", 
                                adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentState());
                    } else {
                        log.warn("❌ Admin assignment failed (collision): {} for room {}", adminCode, chatRoom.getRoomCode());
                        throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
                    }
                } catch (Exception e) {
                    log.error("🚨 Exception during admin assignment: {}", e.getMessage(), e);
                    throw e;
                }
            } else if (!chatRoom.getCurrentAdminCode().equals(adminCode)) {
                log.warn("❌ Admin permission denied: {} attempted access to room {} (owned by {})", 
                         adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentAdminCode());
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            } else {
                // Same admin updating activity
                chatRoom.updateAdminActivity();
                needsUpdate = true;
                log.debug("🔧 Admin activity updated: {} for room {} - STATE: {}", 
                         adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentState());
            }
            
            // Save to MongoDB and update Redis cache when changes occur
            log.info("🔧 needsUpdate check - needsUpdate: {}, room: {}, adminCode: {}", 
                    needsUpdate, chatRoom.getRoomCode(), adminCode);
            if (needsUpdate) {
                ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
                chatCacheService.cacheChatRoom(chatRoom.getRoomCode(), savedRoom);
                log.info("🔧 ChatRoom saved and cached - room: {}, adminCode: {}", 
                        chatRoom.getRoomCode(), adminCode);
            } else {
                log.warn("🚨 needsUpdate is false - ChatRoom NOT saved! room: {}, adminCode: {}", 
                        chatRoom.getRoomCode(), adminCode);
            }
        } catch (Exception e) {
            log.error("🚨 CRITICAL ERROR in assignAdminIfNeeded - room: {}, adminCode: {}, error: {}", 
                     chatRoom.getRoomCode(), adminCode, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * JWT 기반 관리자 코드 결정
     */
    @Override
    public String determineAdminCode(Long memberId, String loginType) {
        log.info("🔍 determineAdminCode called - memberId: {}, loginType: {}", memberId, loginType);
        try {
            if ("ADMIN_CODE".equals(loginType)) {
                // ADMIN_CODE 로그인의 경우 memberId는 AdminCode.id
                AdminCode adminCode = adminCodeRepository.findById(memberId)
                    .orElseThrow(() -> {
                        log.error("🚨 AdminCode를 찾을 수 없습니다 - id: {}", memberId);
                        return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
                    });
                
                log.info("✅ AdminCode 결정 완료 - id: {}, code: {}", memberId, adminCode.getCode());
                return adminCode.getCode();
            } else {
                log.info("✅ Super Admin 코드 설정 - memberId: {}", memberId);
                return "SUPER_ADMIN";
            }
        } catch (Exception e) {
            log.error("🚨 determineAdminCode 실패 - memberId: {}, loginType: {}, error: {}", memberId, loginType, e.getMessage());
            throw e;
        }
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
    
}