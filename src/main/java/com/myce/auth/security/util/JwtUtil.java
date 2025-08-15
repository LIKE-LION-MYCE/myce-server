package com.myce.auth.security.util;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String BEARER_PREFIX = "Bearer ";
    @Value("${jwt.access-token-validity}")
    private long ACCESS_TOKEN_TIME;
    @Value("${jwt.refresh-token-validity}")
    private long REFRESH_TOKEN_TIME;
    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );

        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(BEARER_PREFIX.length());
        }

        log.error("Not Found Token");
        throw new IllegalArgumentException("Not Found Token");
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature.", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
        }

        return false;
    }

    public boolean isExpired(String token) {
        return getExpirationTime(token).before(new Date());
    }

    public long getRemainingTimeForExpiration(String token) {
        return getExpirationTime(token).getTime() - System.currentTimeMillis();
    }

    private Date getExpirationTime(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public String createToken(String category, String loginType, Long id, String loginId, String role) {
        Date now = new Date(System.currentTimeMillis());
        Date expired = category.equals(ACCESS_TOKEN) ?
                new Date(now.getTime() + ACCESS_TOKEN_TIME) :
                new Date(now.getTime() + REFRESH_TOKEN_TIME);

        String jwt = Jwts.builder()
                .claim("category", category)
                .claim("loginType", loginType)
                .claim("memberId", id)
                .claim("loginId", loginId)
                .claim("role", role)
                .signWith(secretKey)
                .expiration(expired)
                .issuedAt(now)
                .compact();

        return category.equals(ACCESS_TOKEN) ? BEARER_PREFIX + jwt : jwt;
    }

    public String getLoginIdFromToken(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("loginId", String.class);
    }

    public Long getMemberIdFromToken(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("memberId", Long.class);
    }

    public String getLoginTypeFromToken(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("loginType", String.class);
    }

    public boolean isRefreshToken(String token) {
        return getCategoryFromToken(token).equals(REFRESH_TOKEN);
    }

    private String getCategoryFromToken(String token) {
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("category", String.class);
    }

    public long getRefreshTokenTime() {
        return REFRESH_TOKEN_TIME;
    }
}
