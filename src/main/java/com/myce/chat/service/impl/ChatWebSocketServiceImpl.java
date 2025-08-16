package com.myce.chat.service.impl;

import com.myce.auth.security.util.JwtUtil;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
                if (!isExpoOwner) {
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
    public MessageResponse sendMessage(Long userId, String roomId, String content) {
        log.warn("🎭 SENDMESSAGE 시작 - userId: {}, roomId: {}, content: '{}'", userId, roomId, content);
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
            
            Optional<AdminCode> adminCodeOpt = adminCodeRepository.findById(userId);
            
            if (adminCodeOpt.isPresent()) {
                senderRole = "ADMIN";
                senderName = "박람회 관리자";
            } else {
                Optional<Member> memberOpt = memberRepository.findById(userId);
                
                if (memberOpt.isPresent()) {
                    Member sender = memberOpt.get();
                    boolean isExpoOwner = expoRepository.existsByIdAndMemberId(expoId, userId);
                    
                    if (isExpoOwner) {
                        senderRole = "ADMIN";
                        senderName = "박람회 관리자";
                    } else {
                        senderRole = "USER";
                        senderName = sender.getName();
                    }
                } else {
                    throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
                }
            }
        }
        
        ChatMessage chatMessage = chatMessageService.createMessage(
            roomId, senderRole, userId, senderName, content
        );
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        updateChatRoomLastMessage(roomId, savedMessage.getId(), content);
        
        return ChatMessageMapper.toSendResponse(savedMessage, roomId);
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
        
        if (!chatRoom.hasAssignedAdmin()) {
            log.info("🔧 No admin assigned, attempting to assign: {}", adminCode);
            // Atomic assignment with collision protection
            boolean assigned = chatRoom.assignAdmin(adminCode);
            if (assigned) {
                chatRoom.setAdminDisplayName(getAdminDisplayName(adminCode));
                log.info("✅ Admin assigned successfully: {} to room {} - NEW STATE: {}", 
                        adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentState());
            } else {
                log.warn("❌ Admin assignment failed (collision): {} for room {}", adminCode, chatRoom.getRoomCode());
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        } else if (!chatRoom.getCurrentAdminCode().equals(adminCode)) {
            log.warn("❌ Admin permission denied: {} attempted access to room {} (owned by {})", 
                     adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentAdminCode());
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        } else {
            // Same admin updating activity
            chatRoom.updateAdminActivity();
            log.debug("🔧 Admin activity updated: {} for room {} - STATE: {}", 
                     adminCode, chatRoom.getRoomCode(), chatRoom.getCurrentState());
        }
    }

    /**
     * JWT 기반 관리자 코드 결정
     */
    @Override
    public String determineAdminCode(Long memberId, String loginType) {
        if ("ADMIN_CODE".equals(loginType)) {
            AdminCode adminCode = adminCodeRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            return adminCode.getCode();
        } else {
            return "SUPER_ADMIN";
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