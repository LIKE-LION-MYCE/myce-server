package com.myce.auth.security.config;

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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenCookieProvider tokenCookieProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AdminCodeDetailService adminCodeDetailService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final MemberAuthenticationProvider memberAuthenticationProvider;
    private final AdminAuthenticationProvider adminAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(
                memberAuthenticationProvider,
                adminAuthenticationProvider
        );
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        LoginFilter loginFilter = new LoginFilter(jwtUtil, tokenCookieProvider,
                authenticationManager());
        loginFilter.setFilterProcessesUrl("/api/auth/login");

        http.cors(cors ->
                        cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable) // CSRF 공격 방지 기능 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, adminCodeDetailService),
                LoginFilter.class).addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/auth/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ads")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/auth-docs/login",
                                "/actuator/health",
                                "/actuator/info",
                                "/ws/**" // WebSocket 엔드포인트 허용
                        ).permitAll()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }
}


