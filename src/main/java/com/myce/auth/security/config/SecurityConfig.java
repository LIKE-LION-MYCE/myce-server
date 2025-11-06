package com.myce.auth.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.myce.auth.repository.impl.OAuth2AuthorizationRequestRepositoryImpl;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AdminCodeDetailService;
import com.myce.auth.service.impl.UserDetailsServiceImpl;
import com.myce.common.exception.ErrorResponse;
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
    private final OAuth2AuthorizationRequestRepositoryImpl oauth2AuthorizationRequestRepository;

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
                            //  HTTP 상태 코드 설정
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            // Content-Type을 JSON으로 설정
                            response.setContentType("application/json;charset=UTF-8");
                            // ErrorResponse 객체 생성
                            ErrorResponse errorResponse = new ErrorResponse("U002", "유효하지 않은 토큰입니다.");
                            // 객체를 JSON으로 직렬화하여 응답 본문에 작성
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
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
                        .authorizationRequestRepository(oauth2AuthorizationRequestRepository))
                .redirectionEndpoint(redirect -> redirect
                        .baseUri("/api/login/oauth2/code/*"))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler));

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.POST, SecurityEndpoints.POST_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityEndpoints.GET_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.PATCH, SecurityEndpoints.PATCH_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, SecurityEndpoints.DELETE_PERMIT_ALL)
                        .permitAll()
                        .requestMatchers(SecurityEndpoints.ETC_PERMIT_ALL).permitAll()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }
}


