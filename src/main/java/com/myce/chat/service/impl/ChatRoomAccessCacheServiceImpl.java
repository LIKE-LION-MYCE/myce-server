package com.myce.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.chat.service.ChatRoomAccessCacheService;
import com.myce.member.entity.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

/**
 * 채팅방 접근 권한 캐시 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomAccessCacheServiceImpl implements ChatRoomAccessCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 프리픽스 상수
    private static final String ACCESS_CACHE_KEY_PREFIX = "chat:access:";
    private static final String USER_ACCESS_PATTERN = "chat:access:user:";
    private static final String ROOM_ACCESS_PATTERN = "chat:access:room:";

    // 캐시 설정 상수
    private static final Duration ACCESS_CACHE_TTL = Duration.ofMinutes(10); // 10분 TTL

    @Override
    public ChatRoomAccessInfo getCachedAccessInfo(String roomCode, Long userId, Role userRole) {
        try {
            String cacheKey = buildAccessCacheKey(roomCode, userId, userRole);
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                log.debug("🔍 Raw cached data type: {}, value: {}", cachedData.getClass().getSimpleName(), cachedData);
                
                ChatRoomAccessInfo accessInfo;
                if (cachedData instanceof String) {
                    // String 형태로 저장된 경우 JSON 파싱
                    accessInfo = objectMapper.readValue((String) cachedData, ChatRoomAccessInfo.class);
                } else {
                    // Object 형태로 저장된 경우 convertValue 사용
                    accessInfo = objectMapper.convertValue(cachedData, ChatRoomAccessInfo.class);
                }
                
                log.debug("🚀 권한 캐시 히트 - roomCode: {}, userId: {}, accessInfo: {}", 
                         roomCode, userId, accessInfo.toString());
                return accessInfo;
            }
            
            log.debug("🔍 권한 캐시 미스 - roomCode: {}, userId: {}, key: {}", roomCode, userId, cacheKey);
            return null;
            
        } catch (Exception e) {
            log.error("권한 캐시 조회 실패 - roomCode: {}, userId: {}, error: {}", 
                    roomCode, userId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void cacheAccessInfo(String roomCode, Long userId, Role userRole, ChatRoomAccessInfo accessInfo) {
        try {
            String cacheKey = buildAccessCacheKey(roomCode, userId, userRole);
            
            // JSON 문자열로 직렬화하여 저장 (더 안정적인 방식)
            String jsonValue = objectMapper.writeValueAsString(accessInfo);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, ACCESS_CACHE_TTL);
            
            log.debug("✅ 권한 캐시 저장 - roomCode: {}, userId: {}, key: {}, accessInfo: {}", 
                     roomCode, userId, cacheKey, accessInfo.toString());
            log.debug("📝 저장된 JSON: {}", jsonValue);
                     
        } catch (Exception e) {
            log.error("권한 캐시 저장 실패 - roomCode: {}, userId: {}, error: {}", 
                    roomCode, userId, e.getMessage(), e);
        }
    }

    @Override
    public void invalidateUserAccessCache(Long userId) {
        try {
            String pattern = USER_ACCESS_PATTERN + userId + ":*";
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("🗑️ 사용자 권한 캐시 무효화 완료 - userId: {}, 삭제된 키: {}", userId, keysToDelete.size());
            }
            
        } catch (Exception e) {
            log.warn("사용자 권한 캐시 무효화 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    @Override
    public void invalidateRoomAccessCache(String roomCode) {
        try {
            String pattern = ROOM_ACCESS_PATTERN + roomCode + ":*";
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("🗑️ 채팅방 권한 캐시 무효화 완료 - roomCode: {}, 삭제된 키: {}", roomCode, keysToDelete.size());
            }
            
        } catch (Exception e) {
            log.warn("채팅방 권한 캐시 무효화 실패 - roomCode: {}, error: {}", roomCode, e.getMessage());
        }
    }

    /**
     * 권한 캐시 키 생성
     * 패턴: chat:access:user:{userId}:room:{roomCode}:{userRole}
     */
    private String buildAccessCacheKey(String roomCode, Long userId, Role userRole) {
        return String.format("%suser:%d:room:%s:%s", 
                           ACCESS_CACHE_KEY_PREFIX, userId, roomCode, userRole.name());
    }
}