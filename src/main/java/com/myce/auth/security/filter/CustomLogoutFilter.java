package com.myce.auth.security.filter;

import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.repository.TokenBlackListRepository;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final TokenCookieProvider tokenCookieProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if(!(request.getMethod().equals("POST") && request.getRequestURI().equals("/api/auth/logout"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // accessToken 검증 및 블랙리스트 추가
        String accessToken = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        accessToken = jwtUtil.substringToken(accessToken);
        if(accessToken == null || !jwtUtil.validateToken(accessToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        String loginType = jwtUtil.getLoginTypeFromToken(accessToken);
        Long memberId = jwtUtil.getMemberIdFromToken(accessToken);

        long time = jwtUtil.getRemainingTimeForExpiration(accessToken);
        tokenBlackListRepository.save(accessToken, time);

        // refreshToken 삭제
        String refreshToken = getRefreshToken(request.getCookies());
        if(refreshToken != null) {
            refreshTokenRepository.deleteByLoginTypeAndMemberId(loginType, memberId);
        }

        // 쿠키 초기화
        ResponseCookie cookie = tokenCookieProvider.getExpiredCookie(JwtUtil.REFRESH_TOKEN);
        response.setHeader("Set-Cookie", cookie.toString());

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        log.info("[LOGOUT-{}] Received login request. memberId: {}", loginType, memberId);
    }

    private String getRefreshToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JwtUtil.REFRESH_TOKEN)) {
                return cookie.getValue();
            }
        }

        throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
    }
}
