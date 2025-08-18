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

    private static final String KEY_FORMAT = "reservation:pre:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(PreReservationCacheDto cacheDto, int limitTime) {
        // reservationCode를 키로 사용
        String key = String.format(KEY_FORMAT, cacheDto.getReservationCode());
        redisTemplate.opsForValue().set(key, cacheDto, limitTime, TimeUnit.MINUTES);
    }

    public PreReservationCacheDto findByReservationCode(String reservationCode) {
        String key = String.format(KEY_FORMAT, reservationCode);
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        return objectMapper.convertValue(result, PreReservationCacheDto.class);
    }
    
    public void deleteByReservationCode(String reservationCode) {
        String key = String.format(KEY_FORMAT, reservationCode);
        redisTemplate.delete(key);
    }

    @Override
    public PreReservationCacheDto findById(Long id) {
        // 레거시 호환성을 위해 유지
        String key = String.format(KEY_FORMAT, id.toString());
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        return objectMapper.convertValue(result, PreReservationCacheDto.class);
    }
    
    @Override
    public void delete(Long id) {
        // 레거시 호환성을 위해 유지
        String key = String.format(KEY_FORMAT, id.toString());
        redisTemplate.delete(key);
    }
}