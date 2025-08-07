package com.myce.chat.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * CRM-186: 채팅방 목록 조회 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /**
     * 사용자별 채팅방 목록 조회 (USER: 본인 참여, ADMIN: 관리 박람회 전체)
     */
    @GetMapping
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("=== 채팅방 목록 조회 API 호출 시작 ===");
        if (customUserDetails == null) {
            log.error("CustomUserDetails가 null입니다. 인증되지 않은 사용자");
            return ResponseEntity.status(403).build();
        }
        
        log.info("채팅방 목록 조회 API 호출 - 회원ID: {}, 역할: {}", 
                customUserDetails.getMemberId(), customUserDetails.getRole());

        ChatRoomListResponse response = chatRoomService.getChatRooms(
                customUserDetails.getMemberId(), 
                customUserDetails.getRole()
        );

        log.info("채팅방 목록 조회 성공 - 회원ID: {}, 조회된 채팅방 수: {}", 
                customUserDetails.getMemberId(), response.getTotalCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 박람회별 채팅방 목록 조회 (관리자 전용, 권한 검증 포함)
     */
    @GetMapping("/expo/{expoId}")
    public ResponseEntity<ChatRoomListResponse> getChatRoomsByExpo(
            @PathVariable("expoId") Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("박람회별 채팅방 조회 API 호출 - 박람회ID: {}, 관리자ID: {}", 
                expoId, customUserDetails.getMemberId());

        ChatRoomListResponse response = chatRoomService.getChatRoomsByExpo(
                expoId, 
                customUserDetails.getMemberId()
        );

        log.info("박람회별 채팅방 조회 성공 - 박람회ID: {}, 관리자ID: {}, 채팅방 수: {}", 
                expoId, customUserDetails.getMemberId(), response.getTotalCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 메시지 히스토리 조회 (페이징)
     */
    @GetMapping("/{roomCode}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable("roomCode") String roomCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("메시지 히스토리 조회 API 호출 - roomCode: {}, 회원ID: {}, page: {}, size: {}", 
                roomCode, customUserDetails.getMemberId(), page, size);

        // 페이징 설정 (최대 100개로 제한)
        int pageSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, pageSize);
        
        // TODO: 채팅방 접근 권한 검증 로직 추가 필요
        // 현재는 인증된 사용자라면 모든 채팅방 메시지 조회 가능
        
        PageResponse<MessageResponse> response = chatMessageService.getMessages(roomCode, pageable);
        
        log.info("메시지 히스토리 조회 성공 - roomCode: {}, 회원ID: {}, 조회된 메시지 수: {}", 
                roomCode, customUserDetails.getMemberId(), response.content().size());

        return ResponseEntity.ok(response);
    }
}