package com.myce.chat.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.service.ChatMessageService;
import com.myce.chat.service.ChatRoomService;
import com.myce.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        
        if (customUserDetails == null) {
            log.error("CustomUserDetails가 null입니다. 인증되지 않은 사용자");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        

        ChatRoomListResponse response = chatRoomService.getChatRooms(
                customUserDetails.getMemberId(),
                customUserDetails.getRole()
        );


        return ResponseEntity.ok(response);
    }

    /**
     * 박람회별 채팅방 목록 조회 (관리자 전용, 권한 검증 포함)
     */
    @GetMapping("/expo/{expoId}")
    public ResponseEntity<ChatRoomListResponse> getChatRoomsByExpo(
            @PathVariable("expoId") Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        

        ChatRoomListResponse response = chatRoomService.getChatRoomsByExpo(
                expoId,
                customUserDetails.getMemberId()
        );


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
        

        // 페이징 설정 (최대 1000개로 제한)
        int pageSize = Math.min(size, 1000);
        Pageable pageable = PageRequest.of(page, pageSize);

        // 채팅방 접근 권한 검증 로직 추가
        chatRoomService.validateChatRoomAccess(roomCode, customUserDetails.getMemberId(), customUserDetails.getRole());

        PageResponse<MessageResponse> response = chatMessageService.getMessages(roomCode, pageable);
        

        return ResponseEntity.ok(response);
    }
    
    /**
     * 사용자 채팅방 읽음 처리 API
     */
    @PostMapping("/{roomCode}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("roomCode") String roomCode,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("🔴 ChatRoomController.markAsRead called - roomCode: {}", roomCode);
        
        // ChatRoomService의 markAsRead 메서드 호출 (예외는 GlobalExceptionHandler에서 처리)
        chatRoomService.markAsRead(roomCode, null, customUserDetails.getMemberId(), customUserDetails.getRole());
        
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회 API
     * 역할 기반 접근 제어: USER는 본인 방만, ADMIN은 관리 권한 있는 방만
     */
    @GetMapping("/{roomCode}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable("roomCode") String roomCode,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("🔵 ChatRoomController.getUnreadCount called - roomCode: {}, role: {}", 
                roomCode, customUserDetails.getRole());
        
        // ChatRoomService를 통한 역할 기반 unread count 조회
        Long unreadCount = chatRoomService.getUnreadCount(roomCode, customUserDetails.getMemberId(), customUserDetails.getRole());
        
        return ResponseEntity.ok(unreadCount);
    }
}