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

    /**
     * 채팅방 접근 권한 캐시 조회
     *
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @return 캐시된 접근 권한 정보, 없으면 null
     */
    @Override
    public ChatRoomAccessInfo getCachedAccessInfo(String roomCode, Long userId, Role userRole) {
        try {
            String cacheKey = buildAccessCacheKey(roomCode, userId, userRole);
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                log.debug("Cached data type: {}, value: {}", cachedData.getClass().getSimpleName(), cachedData);

                ChatRoomAccessInfo accessInfo;
                if (cachedData instanceof String) {
                    // String 형태로 저장된 경우 JSON 파싱
                    accessInfo = objectMapper.readValue((String) cachedData, ChatRoomAccessInfo.class);
                } else {
                    // Object 형태로 저장된 경우 convertValue 사용
                    accessInfo = objectMapper.convertValue(cachedData, ChatRoomAccessInfo.class);
                }

                log.debug("Access permission cache hit - roomCode: {}, userId: {}, accessInfo: {}",
                         roomCode, userId, accessInfo.toString());
                return accessInfo;
            }

            log.debug("Access permission cache miss - roomCode: {}, userId: {}, key: {}", roomCode, userId, cacheKey);
            return null;
            
        } catch (Exception e) {
            log.error("권한 캐시 조회 실패 - roomCode: {}, userId: {}, error: {}", 
                    roomCode, userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 채팅방 접근 권한 캐싱
     *
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @param accessInfo 캐싱할 접근 권한 정보
     */
    @Override
    public void cacheAccessInfo(String roomCode, Long userId, Role userRole, ChatRoomAccessInfo accessInfo) {
        try {
            String cacheKey = buildAccessCacheKey(roomCode, userId, userRole);
            
            // JSON 문자열로 직렬화하여 저장 (더 안정적인 방식)
            String jsonValue = objectMapper.writeValueAsString(accessInfo);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, ACCESS_CACHE_TTL);
            
            log.debug("Access permission cached - roomCode: {}, userId: {}, key: {}, accessInfo: {}",
                     roomCode, userId, cacheKey, accessInfo.toString());
                     
        } catch (Exception e) {
            log.error("권한 캐시 저장 실패 - roomCode: {}, userId: {}, error: {}", 
                    roomCode, userId, e.getMessage(), e);
        }
    }

    /**
     * 사용자별 접근 권한 캐시 무효화
     *
     * @param userId 사용자 ID
     */
    @Override
    public void invalidateUserAccessCache(Long userId) {
        try {
            String pattern = USER_ACCESS_PATTERN + userId + ":*";
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("User access cache invalidated - userId: {}, deleted keys: {}", userId, keysToDelete.size());
            }
            
        } catch (Exception e) {
            log.warn("사용자 권한 캐시 무효화 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    /**
     * 채팅방별 접근 권한 캐시 무효화
     *
     * @param roomCode 채팅방 코드
     */
    @Override
    public void invalidateRoomAccessCache(String roomCode) {
        try {
            String pattern = ROOM_ACCESS_PATTERN + roomCode + ":*";
            Set<String> keysToDelete = redisTemplate.keys(pattern);
            
            if (keysToDelete != null && !keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("Room access cache invalidated - roomCode: {}, deleted keys: {}", roomCode, keysToDelete.size());
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