package com.myce.chat.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.chat.dto.ChatReadRequest;
import com.myce.chat.service.ExpoChatService;
import com.myce.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 박람회 관리자 채팅 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/expos/{expoId}/chats")
@RequiredArgsConstructor
public class ExpoChatController {

    private final ExpoChatService chatService;

    /**
     * 채팅방 목록 조회 (관리자용)
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListResponse>> getChatRooms(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        
        List<ChatRoomListResponse> rooms = chatService.getChatRoomsForAdmin(expoId, userDetails);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 채팅방 메시지 조회
     */
    @GetMapping("/rooms/{roomCode}/messages")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable Long expoId,
            @PathVariable String roomCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        PageResponse<MessageResponse> messages = chatService.getMessages(
                expoId, roomCode, pageable, userDetails);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 읽음 처리
     */
    @PostMapping("/rooms/{roomCode}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long expoId,
            @PathVariable String roomCode,
            @RequestBody ChatReadRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("🟢 ExpoChatController.markAsRead called - expoId: {}, roomCode: {}", expoId, roomCode);
        chatService.markAsRead(expoId, roomCode, request.getLastReadMessageId(), userDetails);
        return ResponseEntity.ok().build();
    }

    /**
     * 안읽은 메시지 수 조회
     */
    @GetMapping("/rooms/{roomCode}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long expoId,
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long unreadCount = chatService.getUnreadCount(expoId, roomCode, userDetails);
        return ResponseEntity.ok(unreadCount);
    }
}