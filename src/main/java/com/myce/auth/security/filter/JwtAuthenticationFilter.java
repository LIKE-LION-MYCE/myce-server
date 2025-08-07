package com.myce.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.impl.UserDetailsServiceImpl;
import com.myce.common.exception.CustomException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String REISSUE_URI = "/api/auth/reissue";
    private static final String INVALID_TOKEN_CODE = "INVALID_TOKEN";
    private static final String EXPIRED_TOKEN_CODE = "EXPIRED_TOKEN";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        String uri = request.getRequestURI();
        log.debug("[JwtAuthenticationFilter] Input uri={}", uri);

        // jwt 존재 여부 및 유효성 검사
        if (uri.equals(REISSUE_URI) || token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰만 추출
        String accessToken = jwtUtil.substringToken(token);

        // 토큰 검증
        if (!validateToken(response, accessToken)) {
            return;
        }

        // 사용자 정보 확인
        String loginId = jwtUtil.getLoginIdFromToken(accessToken);
        log.info("JWT find userDetails by loginId: {}", loginId);
        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(loginId);
        } catch (CustomException ce) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 다음 필터로
        filterChain.doFilter(request, response);
    }

    private boolean validateToken(HttpServletResponse response, String token) throws IOException {
        try {
            if (!jwtUtil.validateToken(token)) {
                setErrorResponse(response, INVALID_TOKEN_CODE);
                return false;
            }

            if (jwtUtil.isExpired(token)) {
                setErrorResponse(response, EXPIRED_TOKEN_CODE);
                return false;
            }
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, EXPIRED_TOKEN_CODE);
            return false;
        }

        return true;
    }

    private void setErrorResponse(HttpServletResponse response, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> body = Map.of("code", code);
        log.info("[JwtAuthenticationFilter] Set error response: {}", body);
        new ObjectMapper().writeValue(response.getWriter(), body);
    }
}
