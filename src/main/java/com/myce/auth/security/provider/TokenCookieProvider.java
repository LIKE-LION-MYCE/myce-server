package com.myce.auth.security.provider;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCookieProvider {

    private static final String DOMAIN = ".myce.live";
    private final CheckProductProvider checkProductProvider;

    public ResponseCookie getCookie(String key, String token) {
        boolean isProd = checkProductProvider.isProd();
        return ResponseCookie.from(key, token)
                .httpOnly(true)
                .sameSite(isProd ? "None" : "Lax")
                .secure(isProd)
                .maxAge(Duration.ofDays(14))
                .domain(isProd ? DOMAIN : null)
                .path("/")
                .build();
    }

    public ResponseCookie getExpiredCookie(String key) {
        boolean isProd = checkProductProvider.isProd();
        return ResponseCookie.from(key, "")
                .httpOnly(true)
                .sameSite(isProd ? "None" : "Lax")
                .secure(isProd)
                .maxAge(0)
                .domain(isProd ? DOMAIN : null)
                .path("/")
                .build();
    }
}
