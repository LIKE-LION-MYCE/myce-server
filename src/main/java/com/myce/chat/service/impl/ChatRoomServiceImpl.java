package com.myce.chat.service.impl;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatRoomService;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myce.chat.service.mapper.ChatRoomMapper;
import java.util.List;
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
 * 
 * @author MYCE Team
 * @since 2025-08-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceImpl implements ChatRoomService {

    // MongoDB Repository
    private final ChatRoomRepository chatRoomRepository;
    
    // MySQL Repositories (상대방 정보 조회용)
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;

    /**
     * 현재 로그인한 사용자의 채팅방 목록 조회
     */
    @Override
    public ChatRoomListResponse getChatRooms(Long memberId, String memberRole) {
        // 1. 로깅: 요청 정보 기록
        log.info("채팅방 목록 조회 시작 - 회원ID: {}, 역할: {}", memberId, memberRole);

        // 2. 회원 존재 여부 확인 (예외 처리)
        Member currentMember = memberRepository.findById(memberId)
            .orElseThrow(() -> {
                log.error("존재하지 않는 회원 ID로 채팅방 조회 시도: {}", memberId);
                return new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
            });

        // 3. 역할별 채팅방 조회 로직 분기
        List<ChatRoom> chatRooms;
        
        if ("ADMIN".equals(memberRole)) {
            // 관리자인 경우: 본인이 관리하는 박람회들의 모든 채팅방 조회
            log.debug("관리자 권한으로 채팅방 조회 - 회원ID: {}", memberId);
            chatRooms = getChatRoomsForAdmin(memberId);
        } else {
            // 일반 사용자인 경우: 본인이 참여한 채팅방만 조회
            log.debug("일반 사용자 권한으로 채팅방 조회 - 회원ID: {}", memberId);
            chatRooms = chatRoomRepository.findByMemberIdAndIsActiveTrueOrderByLastMessageAtDesc(memberId);
        }

        // 4. MongoDB Document를 DTO로 변환 (복수형 네이밍 적용)
        List<ChatRoomListResponse.ChatRoomInfo> chatRoomInfos = chatRooms.stream()
            .map(chatRoom -> convertToChatRoomInfo(chatRoom, memberId, memberRole))
            .collect(Collectors.toList());

        // 5. 응답 객체 생성 및 로깅
        ChatRoomListResponse response = ChatRoomListResponse.builder()
            .chatRooms(chatRoomInfos)  // 복수형 네이밍: chatRoomList → chatRooms
            .totalCount(chatRoomInfos.size())
            .build();

        log.info("채팅방 목록 조회 완료 - 회원ID: {}, 조회된 채팅방 개수: {}", memberId, response.getTotalCount());
        
        return response;
    }

    /**
     * 특정 박람회의 채팅방 목록 조회 (관리자 전용)
     */
    @Override
    public ChatRoomListResponse getChatRoomsByExpo(Long expoId, Long adminId) {
        log.info("박람회별 채팅방 조회 시작 - 박람회ID: {}, 관리자ID: {}", expoId, adminId);

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
            .map(chatRoom -> convertToChatRoomInfo(chatRoom, adminId, "ADMIN"))
            .collect(Collectors.toList());

        // 5. 응답 생성
        ChatRoomListResponse response = ChatRoomListResponse.builder()
            .chatRooms(chatRoomInfos)  // 복수형 네이밍
            .totalCount(chatRoomInfos.size())
            .build();

        log.info("박람회별 채팅방 조회 완료 - 박람회ID: {}, 조회된 채팅방 개수: {}", expoId, response.getTotalCount());
        
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
            log.debug("관리자가 소유한 활성 박람회가 없음 - 관리자ID: {}", adminId);
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

        if ("ADMIN".equals(currentMemberRole)) {
            // 현재 사용자가 관리자면 → 상대방은 일반 참가자
            otherMember = memberRepository.findById(chatRoom.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_MEMBER_NOT_FOUND));
            otherMemberRole = "USER";
        } else {
            // 현재 사용자가 일반 사용자면 → 상대방은 박람회 관리자
            Expo expo = expoRepository.findById(chatRoom.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
            otherMember = expo.getMember();
            otherMemberRole = "ADMIN";
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
}