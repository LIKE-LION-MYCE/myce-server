package com.myce.auth.security.provider;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckProductProvider {

    private static final String PRODUCT_PROFILE = "product";
    private final boolean isProd;
    @Getter
    private final String redirectUrl;

    public CheckProductProvider(@Value("${spring.profiles.active}") String profile) {
        this.isProd = profile.equals(PRODUCT_PROFILE);
        this.redirectUrl = isProd ? "https://www.myce.live" : "http://localhost:5173";
    }

    public boolean isProd() {
        return isProd;
    }
}
