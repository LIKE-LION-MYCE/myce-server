package com.myce.auth.repository.impl;

import com.myce.auth.repository.OAuth2AuthorizationRequestRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based OAuth2 Authorization Request Repository
 * Stores OAuth2 state in Redis instead of HTTP session to enable stateless horizontal scaling
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationRequestRepositoryImpl implements OAuth2AuthorizationRequestRepository {

    private static final String AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final String REDIS_KEY_PREFIX = "oauth2:authorization:";
    private static final int STATE_TTL_MINUTES = 5;
    private static final int COOKIE_MAX_AGE_SECONDS = 180; // 3 minutes

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        
        String stateParameter = getStateParameter(request);
        if (!StringUtils.hasText(stateParameter)) {
            log.debug("OAuth2 state parameter not found in request");
            return null;
        }

        String redisKey = REDIS_KEY_PREFIX + stateParameter;
        Object storedObject = redisTemplate.opsForValue().get(redisKey);
        
        if (storedObject == null) {
            log.warn("OAuth2 authorization request not found in Redis for state: {}", stateParameter);
            return null;
        }
        
        // Handle both OAuth2AuthorizationRequest and LinkedHashMap (from Jackson deserialization)
        OAuth2AuthorizationRequest authorizationRequest;
        if (storedObject instanceof OAuth2AuthorizationRequest) {
            authorizationRequest = (OAuth2AuthorizationRequest) storedObject;
        } else if (storedObject instanceof Map) {
            // Reconstruct from Map when Jackson deserializes
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) storedObject;
            authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId((String) map.get("clientId"))
                .authorizationUri((String) map.get("authorizationUri"))
                .redirectUri((String) map.get("redirectUri"))
                .scopes(new HashSet<>((Collection<String>) map.getOrDefault("scopes", new HashSet<>())))
                .state((String) map.get("state"))
                .additionalParameters((Map<String, Object>) map.getOrDefault("additionalParameters", new HashMap<>()))
                .attributes((Map<String, Object>) map.getOrDefault("attributes", new HashMap<>()))
                .authorizationRequestUri((String) map.get("authorizationRequestUri"))
                .build();
        } else {
            log.error("Unexpected type in Redis for OAuth2 state: {}", storedObject.getClass());
            return null;
        }
        
        if (authorizationRequest != null) {
            log.debug("OAuth2 authorization request loaded from Redis for state: {}", stateParameter);
        } else {
            log.warn("OAuth2 authorization request not found in Redis for state: {}", stateParameter);
        }
        
        return authorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        String state = authorizationRequest.getState();
        Assert.hasText(state, "authorizationRequest.state cannot be empty");

        // Store in Redis with TTL
        String redisKey = REDIS_KEY_PREFIX + state;
        redisTemplate.opsForValue().set(redisKey, authorizationRequest, STATE_TTL_MINUTES, TimeUnit.MINUTES);
        
        // Also store state in a cookie for later retrieval
        Cookie stateCookie = new Cookie(AUTHORIZATION_REQUEST_COOKIE_NAME, encodeState(state));
        stateCookie.setPath("/");
        stateCookie.setHttpOnly(true);
        stateCookie.setSecure(request.isSecure()); // Use secure flag in production
        stateCookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(stateCookie);

        log.info("OAuth2 authorization request saved to Redis - state: {}, provider: {}, redirectUri: {}", 
            state, 
            authorizationRequest.getAttributes().get("registration_id"),
            authorizationRequest.getRedirectUri());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest == null) {
            return null;
        }

        String state = authorizationRequest.getState();
        String redisKey = REDIS_KEY_PREFIX + state;
        
        // Remove from Redis
        redisTemplate.delete(redisKey);
        
        // Clear the cookie
        if (response != null) {
            Cookie stateCookie = new Cookie(AUTHORIZATION_REQUEST_COOKIE_NAME, "");
            stateCookie.setPath("/");
            stateCookie.setMaxAge(0);
            response.addCookie(stateCookie);
        }

        log.debug("OAuth2 authorization request removed from Redis for state: {}", state);
        return authorizationRequest;
    }

    private String getStateParameter(HttpServletRequest request) {
        // First check URL parameter (OAuth2 callback)
        String stateParameter = request.getParameter("state");
        if (StringUtils.hasText(stateParameter)) {
            return stateParameter;
        }

        // Fallback to cookie (for removeAuthorizationRequest during initial auth)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName())) {
                    String encodedState = cookie.getValue();
                    if (StringUtils.hasText(encodedState)) {
                        return decodeState(encodedState);
                    }
                }
            }
        }

        return null;
    }

    private String encodeState(String state) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(state.getBytes());
    }

    private String decodeState(String encodedState) {
        try {
            return new String(Base64.getUrlDecoder().decode(encodedState));
        } catch (Exception e) {
            log.error("Failed to decode OAuth2 state from cookie", e);
            return null;
        }
    }
}