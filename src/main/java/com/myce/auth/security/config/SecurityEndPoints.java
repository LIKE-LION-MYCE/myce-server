package com.myce.auth.security.config;

// Spring Security에서 인증 없이 접근 가능한 엔드포인트를 정의하는 설정 클래스
public final class SecurityEndpoints {

    private SecurityEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ===== POST 요청 허용 엔드포인트 =====
    public static final String[] POST_PERMIT_ALL = {
            "/api/auth/**",
            "/api/reservations/**",
            "/api/reservers",
            "/api/reservations/pre-reservation",
            "/api/payment/**",
            "/api/payment/imp-uid",
    };

    // ===== GET 요청 허용 엔드포인트 =====
    public static final String[] GET_PERMIT_ALL = {
            "/api/ads",
            "/api/auth/**",
            "/api/categories",
            "/api/expos",
            "/api/expos/*/congestion",
            "/api/expos/*/tickets/reservations",
            "/api/expos/*/basic",
            "/api/expos/*/bookmark",
            "/api/expos/*/reviews",
            "/api/expos/*/location",
            "/api/expos/*/booths/public",
            "/api/reservations/*/success",
            "/api/reservations/*/pending",
            "/api/reservations/payment-summary",
            "/api/reservations/guest",
            "/api/reviews/expo/*",
            "/api/reviews/*/",
            "/api/reviews/best",
            "/api/expo/fees/active",
            "/api/ad/fees/active",
            "/api/settings/refund-fee/public",
            "/api/settings/ad-fee/active",
            "/api/settings/expo-fee/active",
            "/api/ad-position/dropdown",
    };

    // ===== PATCH 요청 허용 엔드포인트 =====
    public static final String[] PATCH_PERMIT_ALL = {
            "/api/tickets/quantity",
            "/api/reservations/**",
            "/api/reservations/guestId",
            "/api/reservations/*/confirm",
            "/api/payment/*/status",
            "/api/platform/ads/*/status",
    };

    // ===== DELETE 요청 허용 엔드포인트 =====
    public static final String[] DELETE_PERMIT_ALL = {
            "/api/reservations/**",
            "/api/reservations/*",
    };

    // ===== 기타 요청 허용 엔드포인트 (Swagger, Actuator, WebSocket 등) =====
    public static final String[] ETC_PERMIT_ALL = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/auth-docs/login",
            "/actuator/**",
            "/ws/**",
            "/images/**",
            "/api/login/oauth2/code/**",
            "/api/oauth2/**",
    };
}
