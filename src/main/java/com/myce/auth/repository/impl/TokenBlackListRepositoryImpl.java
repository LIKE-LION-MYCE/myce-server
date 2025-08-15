package com.myce.auth.repository.impl;

import com.myce.auth.repository.TokenBlackListRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBlackListRepositoryImpl implements TokenBlackListRepository {

    private static final String TOKEN_BLACKLIST = "TOKEN_BLACKLIST";
    private static final String KEY_FORMAT = "token:blacklist:%s";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String accessToken, long limitTime) {
        String key = String.format(KEY_FORMAT, accessToken);
        redisTemplate.opsForValue().set(key, TOKEN_BLACKLIST, limitTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean containsByAccessToken(String accessToken) {
        String key = String.format(KEY_FORMAT, accessToken);
        Object result = redisTemplate.opsForValue().get(key);
        return result != null;
    }
}
