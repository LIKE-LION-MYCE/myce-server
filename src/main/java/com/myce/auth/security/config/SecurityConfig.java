package com.myce.auth.security.config;

import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.repository.TokenBlackListRepository;
import com.myce.auth.security.filter.CustomLogoutFilter;
import com.myce.auth.security.filter.JwtAuthenticationFilter;
import com.myce.auth.security.filter.LoginFilter;
import com.myce.auth.security.provider.AdminAuthenticationProvider;
import com.myce.auth.security.provider.MemberAuthenticationProvider;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AdminCodeDetailService;
import com.myce.auth.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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

    private final JwtUtil jwtUtil;
    private final TokenCookieProvider tokenCookieProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AdminCodeDetailService adminCodeDetailService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final MemberAuthenticationProvider memberAuthenticationProvider;
    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlackListRepository tokenBlackListRepository;

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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterBefore(logoutFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, LogoutFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/auth/**", "/api/payment/**",
                        "/api/payment/**", "/api/reservations/**", "/api/reservers",
                        "/api/payment/imp-uid")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ads", "/api/auth/**",
                            "/api/categories", "/api/expos/**", "/api/reservations/**",
                            "/api/expo/fees/active", "/api/ad/fees/active",
                            "/api/members/expos/*/payment", "/api/members/ads/*/payment",
                            "/api/reviews/expo/*", "/api/reviews/*/")
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/quantity",
                            "/api/reservations/**", "/api/platform/ads/*/status",
                            "/api/payment/*/status")
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/**", "/api/reservations/**")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/auth-docs/login",
                                "/actuator/**",               // All actuator endpoints for monitoring
                                "/ws/**", // WebSocket 엔드포인트 허용
                                "/images/**" // Static 이미지 리소스 허용
                        ).permitAll()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }
}


