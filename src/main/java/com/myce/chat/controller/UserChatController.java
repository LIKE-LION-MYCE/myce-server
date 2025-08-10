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
}