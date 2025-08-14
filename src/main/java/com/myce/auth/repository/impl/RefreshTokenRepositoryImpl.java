package com.myce.auth.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.repository.RefreshTokenRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private static final String KEY_FORMAT = "token:refresh:%s:%d";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String loginType, Long memberId, String refreshToken, long limitTime) {
        String key = String.format(KEY_FORMAT, loginType, memberId);
        redisTemplate.opsForValue().set(key, refreshToken, limitTime, TimeUnit.MILLISECONDS);
    }

    public String findByLoginTypeAndMemberId(String loginType, Long memberId) {
        String key = String.format(KEY_FORMAT, loginType, memberId);
        return objectMapper.convertValue(redisTemplate.opsForValue().get(key), String.class);
    }

    public void deleteByLoginTypeAndMemberId(String loginType, Long memberId) {
        String key = String.format(KEY_FORMAT, loginType, memberId);
        redisTemplate.delete(key);
    }
}
