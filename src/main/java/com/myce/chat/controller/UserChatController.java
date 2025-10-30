package com.myce.chat.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.service.ExpoChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 사용자용 채팅 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class UserChatController {
    
    private final ExpoChatService chatService;
    
    /**
     * FAB용 전체 읽지 않은 메시지 수 조회
     */
    @GetMapping("/rooms/unread-counts")
    public ResponseEntity<Map<String, Object>> getAllUnreadCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> result = chatService.getAllUnreadCountsForUser(userDetails);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 박람회 채팅방 생성 또는 조회
     * 유저가 박람회 상세 페이지에서 1:1 채팅을 시작할 때 사용
     */
    @PostMapping("/expo/{expoId}/room")
    public ResponseEntity<Map<String, Object>> getOrCreateExpoChatRoom(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info(" UserChatController.getOrCreateExpoChatRoom called - expoId: {}, userId: {}", 
                expoId, userDetails.getMemberId());
        
        Map<String, Object> chatRoom = chatService.getOrCreateExpoChatRoom(expoId, userDetails);
        
        return ResponseEntity.ok(chatRoom);
    }
}