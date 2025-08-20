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
            "/api/reservers", "/api/payment/imp-uid"
    };

    public static final String[] GET_PERMIT_ALL = {
            "/api/ads", "/api/auth/**",
            "/api/categories", "/api/expos/**", "/api/reservations/**",
            "/api/reservations/guest", "/api/expo/fees/active", "/api/ad/fees/active",
            "/api/reviews/expo/*", "/api/reviews/*/", "/api/reviews/best",
            "/api/settings/refund-fee/public", "/api/ad-position/dropdown",
            "/api/settings/ad-fee/active", "/api/settings/expo-fee/active"
    };

    public static final String[] PATCH_PERMIT_ALL = {
            "/api/tickets/quantity",
            "/api/reservations/**", "/api/platform/ads/*/status",
            "/api/payment/*/status"
    };

    public static final String[] DELETE_PERMIT_ALL = {
            "/api/**", "/api/reservations/**"
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
                        .baseUri("/api/oauth2/authorization"))
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


