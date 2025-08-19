package com.myce.reservation.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.reservation.dto.PreReservationCacheDto;
import com.myce.reservation.repository.PreReservationRepository;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PreReservationRepositoryImpl implements PreReservationRepository {

    private static final String KEY_FORMAT = "reservation:pre:%d";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Deprecated
    public void save(PreReservationCacheDto cacheDto, int limitTime) {
        // 더 이상 사용하지 않음 - saveWithUniqueKey 사용
        log.warn("Deprecated save method called - use saveWithUniqueKey instead");
        String sessionId = saveWithUniqueKey(cacheDto, limitTime);
        log.info("Fallback to saveWithUniqueKey - sessionId: {}", sessionId);
    }
    
    // 고유한 세션 ID로 저장하는 새로운 메서드
    public String saveWithUniqueKey(PreReservationCacheDto cacheDto, int limitTime) {
        String sessionId = UUID.randomUUID().toString();
        String key = "reservation:session:" + sessionId;
        redisTemplate.opsForValue().set(key, cacheDto, limitTime, TimeUnit.MINUTES);
        return sessionId;
    }
    
    // 세션 ID로 조회하는 메서드
    public PreReservationCacheDto findBySessionId(String sessionId) {
        String key = "reservation:session:" + sessionId;
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        return objectMapper.convertValue(result, PreReservationCacheDto.class);
    }
    
    // 세션 ID로 삭제하는 메서드
    public void deleteBySessionId(String sessionId) {
        String key = "reservation:session:" + sessionId;
        redisTemplate.delete(key);
    }

    @Override
    public PreReservationCacheDto findById(Long id) {
        String key = String.format(KEY_FORMAT, id);
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        return objectMapper.convertValue(result, PreReservationCacheDto.class);
    }
    
    @Override
    public PreReservationCacheDto findByReservationCode(String reservationCode) {
        // 사용 안함 - ID 0으로 조회
        return findById(0L);
    }
    
    @Override
    public void delete(Long id) {
        String key = String.format(KEY_FORMAT, id);
        redisTemplate.delete(key);
    }
    
    @Override
    public void deleteByReservationCode(String reservationCode) {
        // 사용 안함 - ID 0으로 삭제
        delete(0L);
    }
}