package com.myce.auth.security.config;

import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.repository.TokenBlackListRepository;
import com.myce.auth.security.filter.CustomLogoutFilter;
import com.myce.auth.security.filter.JwtAuthenticationFilter;
import com.myce.auth.security.filter.LoginFilter;
import com.myce.auth.security.filter.OAuth2LoginFailureHandler;
import com.myce.auth.security.filter.OAuth2LoginSuccessHandler;
import com.myce.auth.security.provider.AdminAuthenticationProvider;
import com.myce.auth.security.provider.MemberAuthenticationProvider;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.repository.RedisOAuth2AuthorizationRequestRepository;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AdminCodeDetailService;
import com.myce.auth.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    public static final String[] POST_PERMIT_ALL = {
            "/api/auth/**", "/api/payment/**", "/api/reservations/**",
            "/api/reservers", "/api/payment/imp-uid",
            "/api/reservations/pre-reservation",     // 사전 예매 생성
    };

    public static final String[] GET_PERMIT_ALL = {
            "/api/ads", "/api/auth/**",
            "/api/categories",
            "/api/expos",                           // 박람회 카드 리스트 조회 (검색, 필터링)
            "/api/expos/*/congestion",              // 박람회 실시간 혼잡도 조회
            "/api/expos/*/tickets/reservations",    // 박람회 티켓 조회(예매용)
            "/api/expos/*/basic",                   // 박람회 기본 정보 조회
            "/api/expos/*/bookmark",                // 박람회 찜하기 상태 조회 (비회원도 접근)
            "/api/expos/*/reviews",                 // 박람회 리뷰 정보 조회 (비회원 접근 가능)
            "/api/expos/*/location",                // 박람회 위치 정보 조회
            "/api/expos/*/booths/public",           // 박람회 부스 정보 조회 (공개용)
            "/api/reservations/*/success",           // 예매 성공 정보 조회
            "/api/reservations/*/pending",           // 예매 대기 정보 조회 (가상계좌)
            "/api/reservations/payment-summary",     // 결제 요약 정보 조회
            "/api/reservations/guest",               // 비회원 예매 조회 (이메일 + 예매번호)
            "/api/reservations/guest", "/api/expo/fees/active", "/api/ad/fees/active",
            "/api/reviews/expo/*", "/api/reviews/*/", "/api/reviews/best",
            "/api/querydsl/search/**",              // QueryDSL 기반 동적 검색 API (비회원 허용)
            "/api/settings/refund-fee/public", "/api/ad-position/dropdown",
            "/api/settings/ad-fee/active", "/api/settings/expo-fee/active"
    };

    public static final String[] PATCH_PERMIT_ALL = {
            "/api/tickets/quantity",
            "/api/reservations/**", "/api/platform/ads/*/status",
            "/api/payment/*/status",
            "/api/reservations/guestId",             // 비회원 ID 업데이트
            "/api/reservations/*/confirm",           // 예매 상태 확인으로 변경
    };

    public static final String[] DELETE_PERMIT_ALL = {
            "/api/reservations/**",
            "/api/reservations/*",                   // 예매 삭제 (결제 실패/취소 시)
    };

    public static final String[] ECT_PERMIT_ALL = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/auth-docs/login",
            "/actuator/**",               // All actuator endpoints for monitoring
            "/ws/**", // WebSocket 엔드포인트 허용
            "/images/**", // Static 이미지 리소스 허용
            "/api/login/oauth2/code/**",      // OAuth2 callback endpoints
            "/api/oauth2/**"              // Custom OAuth2 endpoints
    };

    private final JwtUtil jwtUtil;
    private final TokenCookieProvider tokenCookieProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AdminCodeDetailService adminCodeDetailService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final MemberAuthenticationProvider memberAuthenticationProvider;
    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final RedisOAuth2AuthorizationRequestRepository redisOAuth2AuthorizationRequestRepository;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(
                memberAuthenticationProvider,
                adminAuthenticationProvider
        );
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        LoginFilter loginFilter = new LoginFilter
                (jwtUtil, tokenCookieProvider, authenticationManager(), refreshTokenRepository);
        loginFilter.setFilterProcessesUrl("/api/auth/login");

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter
                (jwtUtil, userDetailsService, adminCodeDetailService, tokenBlackListRepository);

        CustomLogoutFilter logoutFilter = new CustomLogoutFilter
                (jwtUtil, refreshTokenRepository, tokenBlackListRepository, tokenCookieProvider);


        http.cors(cors ->
                        cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable) // CSRF 공격 방지 기능 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized");
                        }))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterBefore(logoutFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, LogoutFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                        .baseUri("/api/oauth2/authorization")
                        .authorizationRequestRepository(redisOAuth2AuthorizationRequestRepository))
                .redirectionEndpoint(redirect -> redirect
                        .baseUri("/api/login/oauth2/code/*"))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler));

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.POST, POST_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, PATCH_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, DELETE_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(ECT_PERMIT_ALL).permitAll()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }
}


