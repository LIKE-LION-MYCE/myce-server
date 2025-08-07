package com.myce.auth.security.provider;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCookieProvider {

    private static final String PRODUCT_PROFILE = "product";

    @Value("${spring.profiles.active}")
    private String profile;

    public ResponseCookie getCookie(String key, String token) {
        boolean isProd = this.profile.equals(PRODUCT_PROFILE);
        return ResponseCookie.from(key, token)
                .httpOnly(true)
                .sameSite(isProd ? "None" : "Lax")
                .secure(isProd)
                .maxAge(Duration.ofDays(14))
                .domain(isProd ? ".myce.live" : null)
                .path("/")
                .build();
    }
}
