package com.myce.auth.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.entity.EmailVerificationInfo;
import com.myce.auth.repository.EmailVerificationRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private static final String KEY_FORMAT = "verification:email:%s:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(EmailVerificationInfo emailVerification, String type, int limitTime) {
        String email = emailVerification.getEmail();
        String key = String.format(KEY_FORMAT, type, email);
        redisTemplate.opsForValue().set(key, emailVerification, limitTime, TimeUnit.MINUTES);
    }

    public EmailVerificationInfo findByEmail(String type, String email) {
        String key = String.format(KEY_FORMAT, type, email);
        return objectMapper.convertValue(redisTemplate.opsForValue().get(key), EmailVerificationInfo.class);
    }
}
