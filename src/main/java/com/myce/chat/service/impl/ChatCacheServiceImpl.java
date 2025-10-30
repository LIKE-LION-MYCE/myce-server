package com.myce.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.service.ChatCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 채팅 Redis 캐시 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatCacheServiceImpl implements ChatCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 프리픽스 상수
    private static final String ROOM_RECENT_KEY_PREFIX = "chat:room:";
    private static final String ROOM_RECENT_KEY_SUFFIX = ":recent";
    private static final String ROOM_UNREAD_KEY_SUFFIX = ":unread:";
    private static final String ROOM_LAST_READ_KEY_SUFFIX = ":lastRead:";
    private static final String USER_BADGE_KEY_PREFIX = "chat:user:";
    private static final String USER_BADGE_KEY_SUFFIX = ":badge";
    private static final String USER_ACTIVE_ROOMS_KEY_SUFFIX = ":activeRooms";

    // 캐시 설정 상수
    private static final int MAX_CACHED_MESSAGES = 50;
    private static final Duration CACHE_TTL = Duration.ofDays(7);
    private static final Duration BADGE_TTL = Duration.ofDays(7);

    /**
     * 최근 메시지 캐시 조회
     * 캐시 히트 시 5-10ms, 미스 시 null 반환
     */
    @Override
    public List<ChatMessage> getCachedRecentMessages(String roomCode, int limit) {
        try {
            String key = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_RECENT_KEY_SUFFIX;
            List<Object> cachedObjects = redisTemplate.opsForList().range(key, 0, limit - 1);
            
            if (cachedObjects == null || cachedObjects.isEmpty()) {
                log.debug("Cache miss for room: {}", roomCode);
                return null;
            }
            
            // Object를 ChatMessage로 변환
            List<ChatMessage> messages = cachedObjects.stream()
                .map(obj -> objectMapper.convertValue(obj, ChatMessage.class))
                .collect(Collectors.toList());
            
            log.debug("Cache hit for room: {}, returned {} messages", roomCode, messages.size());
            return messages;
            
        } catch (Exception e) {
            log.error("Error getting cached messages for room: {}", roomCode, e);
            return null;
        }
    }

    /**
     * 최근 메시지 캐싱
     * MongoDB 조회 후 Redis에 저장
     */
    @Override
    @Transactional
    public void cacheRecentMessages(String roomCode, List<ChatMessage> messages) {
        try {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            
            String key = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_RECENT_KEY_SUFFIX;
            
            // 기존 캐시 삭제
            redisTemplate.delete(key);
            
            // 새 메시지 캐싱 (최신 메시지가 앞에 오도록)
            List<Object> messagesToCache = messages.stream()
                .limit(MAX_CACHED_MESSAGES)
                .collect(Collectors.toList());
            
            if (!messagesToCache.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, messagesToCache.toArray());
                redisTemplate.expire(key, CACHE_TTL);
                log.debug("Cached {} messages for room: {}", messagesToCache.size(), roomCode);
            }
            
        } catch (Exception e) {
            log.error("Error caching messages for room: {}", roomCode, e);
        }
    }

    /**
     * 새 메시지를 캐시에 추가 (동기)
     * WebSocket 메시지 전송 시 즉시 캐싱하여 실시간 반영
     */
    @Override
    public CompletableFuture<Void> addMessageToCache(String roomCode, ChatMessage message) {
        try {
            String key = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_RECENT_KEY_SUFFIX;
            
            // 새 메시지를 리스트 앞에 추가 (최신 메시지가 앞에)
            redisTemplate.opsForList().leftPush(key, message);
            
            // 리스트 크기 제한
            redisTemplate.opsForList().trim(key, 0, MAX_CACHED_MESSAGES - 1);
            
            // TTL 갱신
            redisTemplate.expire(key, CACHE_TTL);
            
            log.debug("Real-time message cached - roomCode: {}, messageId: {}", roomCode, message.getId());
            
        } catch (Exception e) {
            log.error("Failed to add message cache - roomCode: {}, error: {}", roomCode, e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 미읽음 카운트 증가
     * 메시지 전송 시 수신자의 미읽음 카운트 증가
     */
    @Override
    @Transactional
    public Long incrementUnreadCount(String roomCode, Long userId) {
        try {
            String unreadKey = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_UNREAD_KEY_SUFFIX + userId;
            Long count = redisTemplate.opsForValue().increment(unreadKey);
            redisTemplate.expire(unreadKey, CACHE_TTL);
            
            log.debug("Incremented unread count for room: {}, user: {}, new count: {}", 
                     roomCode, userId, count);
            return count;
            
        } catch (Exception e) {
            log.error("Error incrementing unread count for room: {}, user: {}", roomCode, userId, e);
            return 0L;
        }
    }

    /**
     * 미읽음 카운트 리셋
     * 채팅방 읽음 처리 시 호출
     */
    @Override
    @Transactional
    public void resetUnreadCount(String roomCode, Long userId) {
        try {
            String unreadKey = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_UNREAD_KEY_SUFFIX + userId;
            redisTemplate.opsForValue().set(unreadKey, 0L, CACHE_TTL);
            
            log.debug("Reset unread count for room: {}, user: {}", roomCode, userId);
            
        } catch (Exception e) {
            log.error("Error resetting unread count for room: {}, user: {}", roomCode, userId, e);
        }
    }

    /**
     * 미읽음 카운트 조회
     * 채팅방 목록 표시 시 사용
     */
    @Override
    public Long getUnreadCount(String roomCode, Long userId) {
        try {
            String unreadKey = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_UNREAD_KEY_SUFFIX + userId;
            Object value = redisTemplate.opsForValue().get(unreadKey);
            
            if (value == null) {
                return 0L;
            }
            
            if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Long) {
                return (Long) value;
            } else {
                return Long.parseLong(value.toString());
            }
            
        } catch (Exception e) {
            log.error("Error getting unread count for room: {}, user: {}", roomCode, userId, e);
            return 0L;
        }
    }

    /**
     * 전체 배지 카운트 증가
     * 사용자의 전체 미읽음 메시지 수
     */
    @Override
    @Transactional
    public Long incrementBadgeCount(Long userId) {
        try {
            String badgeKey = USER_BADGE_KEY_PREFIX + userId + USER_BADGE_KEY_SUFFIX;
            Long count = redisTemplate.opsForValue().increment(badgeKey);
            redisTemplate.expire(badgeKey, BADGE_TTL);
            
            log.debug("Incremented badge count for user: {}, new count: {}", userId, count);
            return count;
            
        } catch (Exception e) {
            log.error("Error incrementing badge count for user: {}", userId, e);
            return 0L;
        }
    }

    /**
     * 전체 배지 카운트 재계산
     * 읽음 처리 후 전체 카운트 재계산
     */
    @Override
    @Transactional
    public Long recalculateBadgeCount(Long userId) {
        try {
            // 사용자의 모든 활성 채팅방 조회
            List<String> activeRooms = getUserActiveRooms(userId);
            
            long totalUnread = 0;
            for (String roomCode : activeRooms) {
                totalUnread += getUnreadCount(roomCode, userId);
            }
            
            // 전체 배지 카운트 업데이트
            String badgeKey = USER_BADGE_KEY_PREFIX + userId + USER_BADGE_KEY_SUFFIX;
            redisTemplate.opsForValue().set(badgeKey, totalUnread, BADGE_TTL);
            
            log.debug("Recalculated badge count for user: {}, total: {}", userId, totalUnread);
            return totalUnread;
            
        } catch (Exception e) {
            log.error("Error recalculating badge count for user: {}", userId, e);
            return 0L;
        }
    }

    /**
     * 전체 배지 카운트 조회
     * FAB 버튼에 표시할 전체 미읽음 수
     */
    @Override
    public Long getBadgeCount(Long userId) {
        try {
            String badgeKey = USER_BADGE_KEY_PREFIX + userId + USER_BADGE_KEY_SUFFIX;
            Object value = redisTemplate.opsForValue().get(badgeKey);
            
            if (value == null) {
                return 0L;
            }
            
            if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Long) {
                return (Long) value;
            } else {
                return Long.parseLong(value.toString());
            }
            
        } catch (Exception e) {
            log.error("Error getting badge count for user: {}", userId, e);
            return 0L;
        }
    }

    /**
     * 마지막 읽은 메시지 ID 저장
     * 읽음 상태 추적용
     */
    @Override
    @Transactional
    public void setLastReadMessageId(String roomCode, Long userId, String messageId) {
        try {
            String key = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_LAST_READ_KEY_SUFFIX + userId;
            redisTemplate.opsForValue().set(key, messageId, CACHE_TTL);
            
            log.debug("Set last read message for room: {}, user: {}, messageId: {}", 
                     roomCode, userId, messageId);
            
        } catch (Exception e) {
            log.error("Error setting last read message for room: {}, user: {}", roomCode, userId, e);
        }
    }

    /**
     * 마지막 읽은 메시지 ID 조회
     */
    @Override
    public String getLastReadMessageId(String roomCode, Long userId) {
        try {
            String key = ROOM_RECENT_KEY_PREFIX + roomCode + ROOM_LAST_READ_KEY_SUFFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
            
        } catch (Exception e) {
            log.error("Error getting last read message for room: {}, user: {}", roomCode, userId, e);
            return null;
        }
    }

    /**
     * 채팅방 캐시 무효화
     * 채팅방 삭제 시 관련 캐시 정리
     */
    @Override
    @Transactional
    public void invalidateRoomCache(String roomCode) {
        try {
            // 채팅방 관련 모든 캐시 삭제
            String pattern = ROOM_RECENT_KEY_PREFIX + roomCode + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated cache for room: {}, deleted {} keys", roomCode, keys.size());
            }
            
        } catch (Exception e) {
            log.error("Error invalidating cache for room: {}", roomCode, e);
        }
    }

    /**
     * 사용자별 활성 채팅방 목록 조회
     * 배지 카운트 재계산 시 사용
     */
    @Override
    public List<String> getUserActiveRooms(Long userId) {
        try {
            String key = USER_BADGE_KEY_PREFIX + userId + USER_ACTIVE_ROOMS_KEY_SUFFIX;
            Set<Object> rooms = redisTemplate.opsForSet().members(key);
            
            if (rooms == null || rooms.isEmpty()) {
                return new ArrayList<>();
            }
            
            return rooms.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting active rooms for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 사용자 활성 채팅방 추가
     * 새 채팅방 생성 시 호출
     */
    @Override
    @Transactional
    public void addUserActiveRoom(Long userId, String roomCode) {
        try {
            String key = USER_BADGE_KEY_PREFIX + userId + USER_ACTIVE_ROOMS_KEY_SUFFIX;
            redisTemplate.opsForSet().add(key, roomCode);
            redisTemplate.expire(key, BADGE_TTL);
            
            log.debug("Added active room for user: {}, room: {}", userId, roomCode);
            
        } catch (Exception e) {
            log.error("Error adding active room for user: {}, room: {}", userId, roomCode, e);
        }
    }

    /**
     * ChatRoom 캐시 조회
     */
    @Override
    public ChatRoom getCachedChatRoom(String roomCode) {
        try {
            String key = "chat:room:" + roomCode;
            Object cachedData = redisTemplate.opsForValue().get(key);
            
            if (cachedData != null) {
                ChatRoom chatRoom;
                if (cachedData instanceof String) {
                    chatRoom = objectMapper.readValue((String) cachedData, ChatRoom.class);
                } else {
                    chatRoom = objectMapper.convertValue(cachedData, ChatRoom.class);
                }
                
                log.debug("ChatRoom cache hit - roomCode: {}", roomCode);
                return chatRoom;
            }
            
            log.debug("ChatRoom cache miss - roomCode: {}", roomCode);
            return null;
            
        } catch (Exception e) {
            log.error("ChatRoom 캐시 조회 실패 - roomCode: {}, error: {}", roomCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * ChatRoom 캐싱
     */
    @Override
    public void cacheChatRoom(String roomCode, ChatRoom chatRoom) {
        try {
            String key = "chat:room:" + roomCode;
            String jsonValue = objectMapper.writeValueAsString(chatRoom);
            
            // 30분 TTL (ChatRoom 정보는 상대적으로 오래 유지)
            redisTemplate.opsForValue().set(key, jsonValue, Duration.ofMinutes(30));
            
            log.debug("ChatRoom cached - roomCode: {}", roomCode);
            
        } catch (Exception e) {
            log.error("ChatRoom 캐시 저장 실패 - roomCode: {}, error: {}", roomCode, e.getMessage(), e);
        }
    }
}