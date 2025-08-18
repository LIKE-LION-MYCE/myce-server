package com.myce.reservation.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.reservation.dto.PreReservationCacheDto;
import com.myce.reservation.repository.PreReservationRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PreReservationRepositoryImpl implements PreReservationRepository {

    private static final String KEY_FORMAT = "reservation:pre:%d";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(PreReservationCacheDto cacheDto, int limitTime) {
        // 임시 ID 0으로 Redis 저장
        String key = String.format(KEY_FORMAT, 0L);
        redisTemplate.opsForValue().set(key, cacheDto, limitTime, TimeUnit.MINUTES);
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