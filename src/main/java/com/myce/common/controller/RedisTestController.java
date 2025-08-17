package com.myce.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/redis-test")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 연결 테스트
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        try {
            // Redis에 테스트 데이터 저장
            String testKey = "test:ping:" + System.currentTimeMillis();
            String testValue = "Redis connection test successful!";
            
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofMinutes(1));
            
            // 저장된 값 조회
            String retrieved = (String) redisTemplate.opsForValue().get(testKey);
            
            // 테스트 키 삭제
            redisTemplate.delete(testKey);
            
            return Map.of(
                "status", "SUCCESS",
                "message", "Redis connection is working!",
                "test_key", testKey,
                "stored_value", testValue,
                "retrieved_value", retrieved,
                "values_match", testValue.equals(retrieved)
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "message", "Redis connection failed",
                "error", e.getMessage()
            );
        }
    }

    /**
     * 채팅 메시지 캐싱 테스트
     */
    @PostMapping("/chat-test")
    public Map<String, Object> testChatCaching(@RequestBody Map<String, Object> request) {
        try {
            String roomCode = (String) request.get("roomCode");
            String message = (String) request.get("message");
            
            if (roomCode == null || message == null) {
                return Map.of(
                    "status", "ERROR",
                    "message", "roomCode and message are required"
                );
            }
            
            // 채팅 메시지 캐시 키 생성
            String cacheKey = "chat:room:" + roomCode + ":recent";
            
            // 메시지를 List에 추가 (최신 메시지가 맨 앞에)
            redisTemplate.opsForList().leftPush(cacheKey, message);
            
            // TTL 설정 (7일)
            redisTemplate.expire(cacheKey, Duration.ofDays(7));
            
            // 최근 5개 메시지 조회
            var recentMessages = redisTemplate.opsForList().range(cacheKey, 0, 4);
            
            return Map.of(
                "status", "SUCCESS",
                "message", "Chat message cached successfully",
                "cache_key", cacheKey,
                "stored_message", message,
                "recent_messages", recentMessages,
                "total_messages", redisTemplate.opsForList().size(cacheKey)
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "message", "Chat caching test failed",
                "error", e.getMessage()
            );
        }
    }

    /**
     * 미읽음 카운트 테스트
     */
    @PostMapping("/unread-test")
    public Map<String, Object> testUnreadCount(@RequestBody Map<String, Object> request) {
        try {
            String roomCode = (String) request.get("roomCode");
            String userId = (String) request.get("userId");
            Integer increment = (Integer) request.getOrDefault("increment", 1);
            
            if (roomCode == null || userId == null) {
                return Map.of(
                    "status", "ERROR",
                    "message", "roomCode and userId are required"
                );
            }
            
            // 미읽음 카운트 키 생성
            String unreadKey = "chat:room:" + roomCode + ":unread:" + userId;
            String badgeKey = "chat:user:" + userId + ":badge";
            
            // 미읽음 카운트 증가
            Long roomUnreadCount = redisTemplate.opsForValue().increment(unreadKey, increment);
            Long totalBadgeCount = redisTemplate.opsForValue().increment(badgeKey, increment);
            
            // TTL 설정 (7일)
            redisTemplate.expire(unreadKey, Duration.ofDays(7));
            redisTemplate.expire(badgeKey, Duration.ofDays(7));
            
            return Map.of(
                "status", "SUCCESS",
                "message", "Unread count updated successfully",
                "room_unread_key", unreadKey,
                "room_unread_count", roomUnreadCount,
                "badge_key", badgeKey,
                "total_badge_count", totalBadgeCount
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "message", "Unread count test failed",
                "error", e.getMessage()
            );
        }
    }

    /**
     * Redis 키 조회 (디버깅용)
     */
    @GetMapping("/keys")
    public Map<String, Object> getKeys(@RequestParam(required = false) String pattern) {
        try {
            String searchPattern = pattern != null ? pattern : "chat:*";
            var keys = redisTemplate.keys(searchPattern);
            
            return Map.of(
                "status", "SUCCESS",
                "pattern", searchPattern,
                "keys", keys,
                "count", keys != null ? keys.size() : 0
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "message", "Failed to retrieve keys",
                "error", e.getMessage()
            );
        }
    }

    /**
     * Redis 키 삭제 (테스트 정리용)
     */
    @DeleteMapping("/cleanup")
    public Map<String, Object> cleanup(@RequestParam(required = false) String pattern) {
        try {
            String searchPattern = pattern != null ? pattern : "test:*";
            var keys = redisTemplate.keys(searchPattern);
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            return Map.of(
                "status", "SUCCESS",
                "message", "Cleanup completed",
                "pattern", searchPattern,
                "deleted_count", keys != null ? keys.size() : 0
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "message", "Cleanup failed",
                "error", e.getMessage()
            );
        }
    }
}