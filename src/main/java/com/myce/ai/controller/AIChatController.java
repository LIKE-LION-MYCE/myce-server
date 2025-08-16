package com.myce.ai.controller;

import com.myce.ai.service.AIChatService;
import com.myce.auth.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 채팅 컨트롤러 - 플랫폼 상담 AI 서비스
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    /**
     * 대화 요약 생성 (관리자 인계용)
     * GET /api/ai/chat/{roomCode}/summary
     */
    @GetMapping("/{roomCode}/summary")
    public ResponseEntity<Map<String, Object>> generateConversationSummary(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("대화 요약 요청 - roomCode: {}, userId: {}", roomCode, userDetails.getMemberId());
            
            // 플랫폼 관리자 권한 확인
            if (!userDetails.getRole().equals("PLATFORM_ADMIN")) {
                return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", "플랫폼 관리자 권한이 필요합니다."
                ));
            }
            
            // 플랫폼 채팅방 확인 (platform-{userId} 형식)
            if (!roomCode.startsWith("platform-")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error", 
                    "message", "플랫폼 채팅방만 요약 가능합니다."
                ));
            }
            
            // AI 서비스로 요약 생성
            String summary = aiChatService.generateConversationSummary(roomCode);
            
            log.info("대화 요약 생성 완료 - roomCode: {}, summaryLength: {}", roomCode, summary.length());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "roomCode", roomCode,
                "summary", summary,
                "generatedAt", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("대화 요약 생성 실패 - roomCode: {}", roomCode, e);
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "대화 요약 생성에 실패했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * AI 채팅 활성화 상태 확인
     * GET /api/ai/chat/{roomCode}/status  
     */
    @GetMapping("/{roomCode}/status")
    public ResponseEntity<Map<String, Object>> getAIChatStatus(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        try {
            boolean isAIEnabled = aiChatService.isAIEnabled(roomCode);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "roomCode", roomCode,
                "aiEnabled", isAIEnabled,
                "roomType", roomCode.startsWith("platform-") ? "platform" : "expo"
            ));
            
        } catch (Exception e) {
            log.error("AI 채팅 상태 확인 실패 - roomCode: {}", roomCode, e);
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error", 
                "message", "AI 채팅 상태 확인에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}