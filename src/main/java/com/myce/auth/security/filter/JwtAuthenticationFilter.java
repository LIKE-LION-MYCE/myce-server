package com.myce.auth.security.filter;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.service.UserDetailsServiceImpl;
import com.myce.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);

        // jwt 존재 여부 및 유효성 검사
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰만 추출
        String accessToken = jwtUtil.substringToken(token);

        if (!jwtUtil.validateToken(accessToken)) {
            log.info("Invalid Jwt token. uri={}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 만료시간 확인
        if (jwtUtil.isExpired(accessToken)) {
            log.info("Expired Jwt token. uri={}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
}
