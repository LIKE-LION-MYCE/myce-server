package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.member.entity.type.Role;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.service.ChatRoomService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.ai.service.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myce.chat.service.mapper.ChatRoomMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * 채팅방 서비스 구현체
 * 
 * 핵심 로직:
 * 1. 권한 기반 채팅방 목록 조회 (일반 사용자 vs 관리자)
 * 2. 읽지 않은 메시지 개수 계산
 * 3. 상대방 정보 매핑 (관리자 ↔ 참가자)
 * 4. 예외 상황 처리 (CustomException 사용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl implements ChatRoomService {

    // MongoDB Repository
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    
    // MySQL Repositories (상대방 정보 조회용)
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final AdminCodeRepository adminCodeRepository;
    
    // WebSocket 메시징
    private final SimpMessagingTemplate messagingTemplate;
    
    // AI 채팅 서비스
    private final AIChatService aiChatService;

    /**
     * 현재 로그인한 사용자의 채팅방 목록 조회
     */
    @Override
    public ChatRoomListResponse getChatRooms(Long memberId, String memberRole) {
        // 1. 로깅: 요청 정보 기록

        // 2. 회원 존재 여부 확인 (예외 처리)
        Member currentMember = memberRepository.findById(memberId)
            .orElseThrow(() -> {
                log.error("존재하지 않는 회원 ID로 채팅방 조회 시도: {}", memberId);
                return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
            });

        // 3. 역할별 채팅방 조회 로직 분기
        List<ChatRoom> chatRooms;
        
        if (Role.EXPO_ADMIN.name().equals(memberRole)) {
            // 박람회 관리자인 경우: 본인이 관리하는 박람회들의 모든 채팅방 조회
            chatRooms = getChatRoomsForAdmin(memberId);
        } else if (Role.PLATFORM_ADMIN.name().equals(memberRole)) {
            // 플랫폼 관리자인 경우: 모든 플랫폼 채팅방 조회 (platform-* rooms)
            chatRooms = chatRoomRepository.findByExpoIdIsNullAndIsActiveTrueOrderByLastMessageAtDesc();
        } else {
            // 일반 사용자인 경우: 본인이 참여한 채팅방만 조회
            chatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(memberId);
            
            // 플랫폼 상담방 자동 생성 (사용자용)
            ensurePlatformRoomExists(memberId);
            
            // 플랫폼 방 포함하여 다시 조회
            chatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(memberId);
        }

        // 4. MongoDB Document를 DTO로 변환 (복수형 네이밍 적용)
        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfos = chatRooms.stream()
            .map(chatRoom -> {
                try {
                    return convertToChatRoomInfo(chatRoom, memberId, memberRole);
                } catch (Exception e) {
                    log.error("채팅방 정보 변환 중 오류 - chatRoom ID: {}, error: {}", 
                        chatRoom.getId(), e.getMessage(), e);
                    return null;
                }
            })
            .filter(info -> info != null)
            .collect(Collectors.toList());

        // 5. 응답 객체 생성 및 로깅
        ChatRoomListResponse response = ChatRoomListResponse.builder()
            .chatRooms(chatRoomInfos)  // 복수형 네이밍: chatRoomList → chatRooms
            .totalCount(chatRoomInfos.size())
            .build();

        
        // 빈 목록일 때도 정상 응답
        if (chatRoomInfos.isEmpty()) {
        }
        
        return response;
    }

    /**
     * 특정 박람회의 채팅방 목록 조회 (관리자 전용)
     */
    @Override
    public ChatRoomListResponse getChatRoomsByExpo(Long expoId, Long adminId) {

        // 1. 박람회 존재 여부 확인
        Expo expo = expoRepository.findById(expoId)
            .orElseThrow(() -> {
                log.error("존재하지 않는 박람회 ID로 채팅방 조회 시도: {}", expoId);
                return new CustomException(CustomErrorCode.EXPO_NOT_EXIST);
            });

        // 2. 권한 검증: AdminCode 권한과 Member 권한을 모두 지원
        boolean hasPermission = false;
        
        // 2-1. AdminCode 권한 확인 시도
        try {
            Optional<AdminCode> adminCodeOpt = adminCodeRepository.findById(adminId);
            if (adminCodeOpt.isPresent()) {
                AdminCode adminCode = adminCodeOpt.get();
                if (adminCode.getExpoId().equals(expoId)) {
                    hasPermission = true;
                    log.info("✅ AdminCode 권한으로 박람회 채팅방 접근 허용 - adminCodeId: {}, expoId: {}", adminId, expoId);
                }
            }
        } catch (Exception e) {
            log.debug("AdminCode 권한 확인 실패, Member 권한으로 시도 - adminId: {}", adminId);
        }
        
        // 2-2. AdminCode 권한이 없으면 Member 권한(expo owner) 확인
        if (!hasPermission) {
            if (expo.getMember().getId().equals(adminId)) {
                hasPermission = true;
                log.info("✅ Member 권한으로 박람회 채팅방 접근 허용 - memberId: {}, expoId: {}", adminId, expoId);
            }
        }
        
        // 2-3. 권한 없으면 예외 발생
        if (!hasPermission) {
            log.error("⚠️ 권한 없는 관리자가 박람회 채팅방 접근 시도 - 박람회ID: {}, 관리자ID: {}", expoId, adminId);
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 3. 해당 박람회의 모든 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(expoId);

        // 4. DTO 변환
        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfos = chatRooms.stream()
            .map(chatRoom -> convertToChatRoomInfo(chatRoom, adminId, Role.EXPO_ADMIN.name()))
            .collect(Collectors.toList());

        // 5. 응답 생성
        ChatRoomListResponse response = ChatRoomListResponse.builder()
            .chatRooms(chatRoomInfos)  // 복수형 네이밍
            .totalCount(chatRoomInfos.size())
            .build();

        
        return response;
    }

    /**
     * 관리자가 관리하는 모든 박람회의 채팅방 조회
     */
    private List<ChatRoom> getChatRoomsForAdmin(Long adminId) {
        // 1. 관리자가 소유한 활성 박람회 조회 (기존 메서드 활용)
        // 우선 첫 번째 활성 박람회만 조회 (기존 메서드 제한으로 인해)
        Optional<Expo> adminExpoOpt = expoRepository.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(
                adminId, ExpoStatus.ACTIVE_STATUSES);
        
        if (adminExpoOpt.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        // 2. 해당 박람회의 채팅방들 조회
        Expo adminExpo = adminExpoOpt.get();
        return chatRoomRepository.findByExpoIdAndIsActiveTrueOrderByLastMessageAtDesc(adminExpo.getId());
    }

    /**
     * ChatRoom Document를 ChatRoomInfo DTO로 변환
     */
    private ChatRoomListResponse.ChatRoomInfo convertToChatRoomInfo(
            ChatRoom chatRoom, Long currentMemberId, String currentMemberRole) {
        
        // 플랫폼 채팅방인지 확인 (expoId가 null인 경우)
        if (chatRoom.getExpoId() == null) {
            // 플랫폼 채팅방 처리 - 실제 사용자 정보 조회
            String actualUserName = chatRoom.getMemberName();
            Long actualUserId = chatRoom.getMemberId();
            
            // 캐시된 memberName이 올바르지 않으면 DB에서 조회
            if (actualUserName == null || 
                actualUserName.contains("AI") || 
                actualUserName.contains("상담사") || 
                actualUserName.equals("플랫폼 사용자")) {
                
                try {
                    Optional<Member> memberOpt = memberRepository.findById(actualUserId);
                    if (memberOpt.isPresent()) {
                        actualUserName = memberOpt.get().getName();
                        log.debug("Platform room user name corrected: {} -> {}", chatRoom.getMemberName(), actualUserName);
                    } else {
                        actualUserName = "사용자 " + actualUserId;
                        log.warn("User not found for platform room: userId={}", actualUserId);
                    }
                } catch (Exception e) {
                    actualUserName = "사용자 " + actualUserId;
                    log.error("Failed to fetch user name for platform room: userId={}, error={}", actualUserId, e.getMessage());
                }
            }
            
            return ChatRoomMapper.toDto(
                    chatRoom,
                    actualUserId,  // Real user ID
                    actualUserName,  // Real user name  
                    "USER",  // User role
                    chatRoom.getExpoTitle(),  // "플랫폼 상담"
                    0  // 읽지 않은 메시지 수
            );
        }
        
        // 1. 상대방 정보 조회 (역할에 따라 다름)
        Member otherMember;
        String otherMemberRole;

        if (Role.EXPO_ADMIN.name().equals(currentMemberRole)) {
            // 현재 사용자가 관리자면 → 상대방은 일반 참가자
            otherMember = memberRepository.findById(chatRoom.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_MEMBER_NOT_FOUND));
            otherMemberRole = Role.USER.name();
        } else {
            // 현재 사용자가 일반 사용자면 → 상대방은 박람회 관리자
            Expo expo = expoRepository.findById(chatRoom.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
            otherMember = expo.getMember();
            otherMemberRole = Role.EXPO_ADMIN.name();
        }

        // 2. 박람회 정보 조회
        Expo expo = expoRepository.findById(chatRoom.getExpoId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 3. 읽지 않은 메시지 개수 계산 (추후 CRM-188에서 구현)
        Integer unreadCount = 0; //

        // 4. ChatRoomInfo DTO 생성 및 반환
        return ChatRoomMapper.toDto(
                chatRoom,
                otherMember.getId(),
                otherMember.getName(),
                otherMemberRole,
                expo.getTitle(),
                unreadCount
        );
    }
    
    /**
     * 사용자 채팅방 읽음 처리 (USER 타입 사용자 전용)
     */
    @Override
    @Transactional
    public void markAsRead(String roomCode, String lastReadMessageId, Long memberId, String memberRole) {
        
        // 1. 채팅방 조회 및 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 채팅방 코드로 읽음 처리 시도 - roomCode: {}", roomCode);
                    return new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
                });
        
        // 2. 사용자 권한 검증 (새로운 통합 권한 검증 로직 사용)
        validateChatRoomAccess(roomCode, memberId, memberRole);
        
        // 3. 마지막 메시지 ID를 가져와서 읽음 처리 (가장 최근 메시지까지 읽음 처리)
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
        if (!recentMessages.isEmpty()) {
            String latestMessageId = recentMessages.get(0).getId();
            
            // 4. readStatusJson 업데이트
            String currentReadStatus = chatRoom.getReadStatusJson();
            String updatedReadStatus = updateReadStatusForUser(currentReadStatus, latestMessageId);
            chatRoom.updateReadStatus(updatedReadStatus);
        }
        
        // 5. 채팅방 저장
        chatRoomRepository.save(chatRoom);
        
        // 6. WebSocket을 통해 관리자에게 읽음 상태 변경 알림
        try {
            Map<String, Object> readStatusPayload = Map.of(
                "roomCode", roomCode,
                "readerType", "USER",
                "unreadCount", 0
            );
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "read_status_update",
                "payload", readStatusPayload
            );
            
            log.info("🔔 USER 읽음 상태 WebSocket 알림 전송 - roomCode: {}, topic: /topic/chat/{}", roomCode, roomCode);
            messagingTemplate.convertAndSend(
                "/topic/chat/" + roomCode,
                broadcastMessage
            );
            
        } catch (Exception e) {
            log.warn("읽음 상태 WebSocket 알림 전송 실패 - roomCode: {}, error: {}", roomCode, e.getMessage());
        }
        
    }
    
    /**
     * 사용자 권한 검증 (본인 채팅방인지 확인)
     */
    private void validateUserPermission(ChatRoom chatRoom, Long memberId, String memberRole) {
        // 플랫폼 관리자는 모든 플랫폼 채팅방에 접근 가능
        if (Role.PLATFORM_ADMIN.name().equals(memberRole) && chatRoom.getExpoId() == null) {
            log.info("플랫폼 관리자가 플랫폼 채팅방 접근 - roomCode: {}, adminId: {}", 
                    chatRoom.getRoomCode(), memberId);
            return; // 플랫폼 관리자는 권한 검증 통과
        }
        
        // 일반 사용자는 본인 채팅방만 접근 가능
        if (!chatRoom.getMemberId().equals(memberId)) {
            log.error("권한 없는 사용자가 채팅방 읽음 처리 시도 - roomCode: {}, 사용자ID: {}, 채팅방 소유자ID: {}", 
                    chatRoom.getRoomCode(), memberId, chatRoom.getMemberId());
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
    
    /**
     * 채팅방 접근 권한 검증 (메시지 조회, 읽음 처리 등에 사용)
     */
    @Override
    public void validateChatRoomAccess(String roomCode, Long memberId, String memberRole) {
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 채팅방 코드로 접근 시도 - roomCode: {}", roomCode);
                    return new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
                });
        
        // 2. 플랫폼 채팅방인 경우
        if (chatRoom.getExpoId() == null) {
            // 플랫폼 관리자는 모든 플랫폼 채팅방 접근 가능
            if (Role.PLATFORM_ADMIN.name().equals(memberRole)) {
                log.info("✅ 플랫폼 관리자가 플랫폼 채팅방 접근 - roomCode: {}, adminId: {}", roomCode, memberId);
                return;
            }
            // 일반 사용자는 본인 채팅방만 접근 가능
            if (chatRoom.getMemberId().equals(memberId)) {
                log.info("✅ 사용자가 본인 플랫폼 채팅방 접근 - roomCode: {}, userId: {}", roomCode, memberId);
                return;
            }
        }
        
        // 3. 박람회 채팅방인 경우
        if (chatRoom.getExpoId() != null) {
            // 3-1. 박람회 관리자 권한 확인 (AdminCode 또는 Owner)
            if (Role.EXPO_ADMIN.name().equals(memberRole)) {
                // AdminCode 권한 확인
                try {
                    Optional<AdminCode> adminCodeOpt = adminCodeRepository.findById(memberId);
                    if (adminCodeOpt.isPresent()) {
                        AdminCode adminCode = adminCodeOpt.get();
                        if (adminCode.getExpoId().equals(chatRoom.getExpoId())) {
                            log.info("✅ AdminCode 권한으로 박람회 채팅방 접근 - roomCode: {}, adminCodeId: {}, expoId: {}", 
                                    roomCode, memberId, chatRoom.getExpoId());
                            return;
                        }
                    }
                } catch (Exception e) {
                    log.debug("AdminCode 권한 확인 실패, Member 권한으로 시도 - adminId: {}", memberId);
                }
                
                // Owner 권한 확인
                try {
                    Expo expo = expoRepository.findById(chatRoom.getExpoId())
                            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
                    if (expo.getMember().getId().equals(memberId)) {
                        log.info("✅ Member 권한으로 박람회 채팅방 접근 - roomCode: {}, memberId: {}, expoId: {}", 
                                roomCode, memberId, chatRoom.getExpoId());
                        return;
                    }
                } catch (Exception e) {
                    log.error("박람회 정보 조회 실패 - expoId: {}", chatRoom.getExpoId(), e);
                }
            }
            
            // 3-2. 일반 사용자는 본인이 참여한 채팅방만 접근 가능
            if (Role.USER.name().equals(memberRole) && chatRoom.getMemberId().equals(memberId)) {
                log.info("✅ 사용자가 본인 박람회 채팅방 접근 - roomCode: {}, userId: {}", roomCode, memberId);
                return;
            }
        }
        
        // 4. 모든 권한 검증 실패
        log.error("⚠️ 권한 없는 사용자가 채팅방 접근 시도 - roomCode: {}, userId: {}, role: {}, expoId: {}", 
                roomCode, memberId, memberRole, chatRoom.getExpoId());
        throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }
    
    /**
     * 사용자 읽음 상태 업데이트
     */
    private String updateReadStatusForUser(String currentReadStatus, String lastReadMessageId) {
        if (currentReadStatus == null || currentReadStatus.isEmpty() || currentReadStatus.equals("{}")) {
            return "{\"USER\":\"" + lastReadMessageId + "\"}";
        }
        
        // 기존 USER 정보가 있으면 업데이트, 없으면 추가
        if (currentReadStatus.contains("\"USER\"")) {
            return currentReadStatus.replaceAll("\"USER\":\"[^\"]*\"", "\"USER\":\"" + lastReadMessageId + "\"");
        } else {
            return currentReadStatus.substring(0, currentReadStatus.length() - 1) + 
                   ",\"USER\":\"" + lastReadMessageId + "\"}";
        }
    }
    
    /**
     * 플랫폼 채팅방 자동 생성
     */
    private void ensurePlatformRoomExists(Long memberId) {
        String platformRoomCode = "platform-" + memberId;
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomCode(platformRoomCode);
        
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
            
            ChatRoom platformRoom = ChatRoom.builder()
                .roomCode(platformRoomCode)
                .expoId(null)  // 플랫폼 방은 expoId 없음
                .memberId(memberId)
                .memberName(memberName)  // Use actual user name
                .expoTitle("플랫폼 상담")    // Frontend에서 이 이름으로 표시됨
                .build();
                
            chatRoomRepository.save(platformRoom);
            log.info("플랫폼 채팅방 자동 생성 - memberId: {}, roomCode: {}, memberName: {}", memberId, platformRoomCode, memberName);
        }
    }
    
    /**
     * AI 상담을 관리자에게 인계 (요약 포함)
     */
    @Override
    @Transactional
    public void handoffAIToAdmin(String roomCode, String adminCode) {
        try {
            // AI 서비스를 통한 인계 처리 (요약 자동 생성)
            aiChatService.handoffToAdmin(roomCode, adminCode);
            
            log.info("AI 상담 관리자 인계 완료 - roomCode: {}, adminCode: {}", roomCode, adminCode);
            
        } catch (Exception e) {
            log.error("AI 상담 관리자 인계 실패 - roomCode: {}, adminCode: {}", roomCode, adminCode, e);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회 (역할 기반 접근 제어)
     */
    @Override
    public Long getUnreadCount(String roomCode, Long memberId, String memberRole) {
        // 1. 채팅방 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 채팅방 - roomCode: {}", roomCode);
                    return new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
                });
        
        // 2. 권한 검증 (새로운 통합 권한 검증 로직 사용)
        validateChatRoomAccess(roomCode, memberId, memberRole);
        
        try {
            // 3. 역할에 따른 unread count 계산
            if (Role.PLATFORM_ADMIN.name().equals(memberRole) || Role.EXPO_ADMIN.name().equals(memberRole)) {
                // 관리자 입장: 사용자가 보낸 메시지 중 읽지 않은 것만 계산
                return calculateUnreadCountForAdmin(chatRoom);
            } else {
                // 일반 사용자 입장: 관리자/AI가 보낸 메시지 중 읽지 않은 것만 계산  
                return calculateUnreadCountForUser(chatRoom);
            }
            
        } catch (Exception e) {
            log.error("읽지 않은 메시지 수 조회 실패 - roomCode: {}, memberId: {}", roomCode, memberId, e);
            return 0L; // 에러 시 0 반환
        }
    }
    
    /**
     * 관리자 입장에서 읽지 않은 메시지 수 계산 (사용자 메시지만)
     */
    private Long calculateUnreadCountForAdmin(ChatRoom chatRoom) {
        String roomCode = chatRoom.getRoomCode();
        String readStatusJson = chatRoom.getReadStatusJson();
        
        // readStatusJson에서 ADMIN의 마지막 읽은 메시지 ID 추출
        String lastReadMessageId = extractLastReadMessageId(readStatusJson, "ADMIN");
        
        if (lastReadMessageId == null || lastReadMessageId.isEmpty()) {
            // 관리자가 아직 아무것도 읽지 않았다면 전체 USER 메시지 개수
            return chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "USER");
        } else {
            // 마지막 읽은 메시지 ID 이후의 USER 메시지 개수
            return chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                roomCode, "USER", lastReadMessageId);
        }
    }
    
    /**
     * 사용자 입장에서 읽지 않은 메시지 수 계산 (관리자/AI 메시지만)
     */
    private Long calculateUnreadCountForUser(ChatRoom chatRoom) {
        String roomCode = chatRoom.getRoomCode();
        String readStatusJson = chatRoom.getReadStatusJson();
        
        // readStatusJson에서 USER의 마지막 읽은 메시지 ID 추출
        String lastReadMessageId = extractLastReadMessageId(readStatusJson, "USER");
        
        if (lastReadMessageId == null || lastReadMessageId.isEmpty()) {
            // 사용자가 아직 아무것도 읽지 않았다면 전체 ADMIN/AI 메시지 개수
            Long adminCount = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "ADMIN");
            Long aiCount = chatMessageRepository.countByRoomCodeAndSenderType(roomCode, "AI");
            return adminCount + aiCount;
        } else {
            // 마지막 읽은 메시지 ID 이후의 ADMIN/AI 메시지 개수
            Long adminCount = chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                roomCode, "ADMIN", lastReadMessageId);
            Long aiCount = chatMessageRepository.countByRoomCodeAndSenderTypeAndIdGreaterThan(
                roomCode, "AI", lastReadMessageId);
            return adminCount + aiCount;
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
}