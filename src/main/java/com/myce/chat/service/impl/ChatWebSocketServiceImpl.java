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

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageService chatMessageService;

    @Override
    public Long authenticateUser(String token) {
        log.debug("WebSocket JWT 토큰 인증 시작");
        
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
            }
            
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtUtil.getMemberIdFromToken(token);
            
            // 사용자 존재 여부 확인
            memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            
            log.debug("WebSocket JWT 인증 성공 - userId: {}", userId);
            return userId;
            
        } catch (Exception e) {
            log.error("WebSocket JWT 인증 실패: {}", e.getMessage());
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }
    }

    @Override
    @Transactional
    public void joinRoom(Long userId, String roomId) {
        log.debug("채팅방 입장 처리 시작 - userId: {}, roomId: {}", userId, roomId);
        
        // roomId 형식 검증: "admin-{expoId}-{userId}"
        if (!isValidRoomIdFormat(roomId)) {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND);
        }
        
        // roomId에서 expoId, participantId 추출
        String[] parts = roomId.split("-");
        Long expoId = Long.parseLong(parts[1]);
        Long participantId = Long.parseLong(parts[2]);
        
        // 사용자 권한 검증
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        
        // 관리자인 경우: 해당 박람회의 소유자인지 확인
        if (Role.EXPO_ADMIN.name().equals(user.getRole().name())) {
            boolean isExpoOwner = expoRepository.existsByIdAndMemberId(expoId, userId);
            if (!isExpoOwner) {
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        } 
        // 일반 사용자인 경우: roomId의 participantId와 일치하는지 확인
        else if (Role.USER.name().equals(user.getRole().name())) {
            if (!userId.equals(participantId)) {
                throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        } else {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        
        // 채팅방 존재 확인 및 생성
        ensureChatRoomExists(roomId, expoId, participantId);
        
        log.info("채팅방 입장 성공 - userId: {}, roomId: {}", userId, roomId);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long userId, String roomId, String content) {
        log.debug("메시지 전송 시작 - userId: {}, roomId: {}", userId, roomId);
        
        // 사용자 정보 조회
        Member sender = memberRepository.findById(userId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
        
        // 메시지 생성 및 저장
        ChatMessage chatMessage = chatMessageService.createMessage(
            roomId,
            sender.getRole().name(),
            userId,
            sender.getName(),
            content
        );
        
        // MongoDB에 메시지 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        
        // 채팅방 마지막 메시지 업데이트
        updateChatRoomLastMessage(roomId, savedMessage.getId(), content);
        
        // 응답 DTO 생성 (Mapper 사용)
        MessageResponse response = ChatMessageMapper.toSendResponse(savedMessage, roomId);
        
        log.info("메시지 전송 완료 - userId: {}, roomId: {}", userId, roomId);
        return response;
    }

    /**
     * roomId 형식 검증
     */
    private boolean isValidRoomIdFormat(String roomId) {
        if (roomId == null || !roomId.startsWith("admin-")) {
            return false;
        }
        
        String[] parts = roomId.split("-");
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
            log.info("새 채팅방 생성 - roomId: {}", roomId);
            
            ChatRoom newRoom = ChatRoom.builder()
                .roomCode(roomId)
                .expoId(expoId)
                .memberId(participantId)
                .build();
                
            chatRoomRepository.save(newRoom);
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
}