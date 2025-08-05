package com.myce.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.info("AttemptAuthentication. URL: {}", request.getRequestURI());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            String loginId = loginRequest.getLoginId();
            String password = loginRequest.getPassword();
            log.info("Get loginId: {}, password: {}", loginId, password);

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginId, password);
            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse login request", e);
        }
    }

    @Override
    protected void successfulAuthentication
            (HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        Long memberId = userDetails.getMemberId();
        String loginId = userDetails.getLoginId();
        String name = userDetails.getUsername();
        String role = userDetails.getRole();

        String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, memberId, loginId, name, role);
        String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, memberId, loginId, name, role);


        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
        Cookie cookie = getCookie(JwtUtil.REFRESH_TOKEN, refreshToken);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
        log.info("Successfully login. loginId: {}", loginId);
    }

    @Override
    protected void unsuccessfulAuthentication
            (HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private Cookie getCookie(String key, String token) {
        Cookie cookie = new Cookie(key, token);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
