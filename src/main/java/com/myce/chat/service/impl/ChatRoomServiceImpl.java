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
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
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
            // 관리자인 경우: 본인이 관리하는 박람회들의 모든 채팅방 조회
            chatRooms = getChatRoomsForAdmin(memberId);
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

        // 2. 권한 검증: 해당 관리자가 이 박람회의 소유자인지 확인
        if (!expo.getMember().getId().equals(adminId)) {
            log.error("권한 없는 관리자가 박람회 채팅방 접근 시도 - 박람회ID: {}, 관리자ID: {}", expoId, adminId);
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
    public void markAsRead(String roomCode, String lastReadMessageId, Long memberId) {
        
        // 1. 채팅방 조회 및 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 채팅방 코드로 읽음 처리 시도 - roomCode: {}", roomCode);
                    return new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
                });
        
        // 2. 사용자 권한 검증 (본인 채팅방인지 확인)
        validateUserPermission(chatRoom, memberId);
        
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
    private void validateUserPermission(ChatRoom chatRoom, Long memberId) {
        if (!chatRoom.getMemberId().equals(memberId)) {
            log.error("권한 없는 사용자가 채팅방 읽음 처리 시도 - roomCode: {}, 사용자ID: {}, 채팅방 소유자ID: {}", 
                    chatRoom.getRoomCode(), memberId, chatRoom.getMemberId());
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
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
            ChatRoom platformRoom = ChatRoom.builder()
                .roomCode(platformRoomCode)
                .expoId(null)  // 플랫폼 방은 expoId 없음
                .memberId(memberId)
                .memberName("플랫폼 사용자")  
                .expoTitle("플랫폼 상담")    // Frontend에서 이 이름으로 표시됨
                .build();
                
            chatRoomRepository.save(platformRoom);
            log.info("플랫폼 채팅방 자동 생성 - memberId: {}, roomCode: {}", memberId, platformRoomCode);
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
}