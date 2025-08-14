package com.myce.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.LoginRequest;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.repository.RefreshTokenRepository;
import com.myce.auth.security.CustomAuthenticationToken;
import com.myce.auth.security.provider.TokenCookieProvider;
import com.myce.auth.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final TokenCookieProvider tokenCookieProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Authentication attemptAuthentication
            (HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("[LoginFilter] AttemptAuthentication. URL: {}", request.getRequestURI());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            LoginType loginType = loginRequest.getLoginType();
            String loginId = loginRequest.getLoginId();
            String password = loginRequest.getPassword();
            log.info("[{}-LOGIN] Received login request. loginId: {}", loginType.name(), loginId);

            CustomAuthenticationToken authRequest = new CustomAuthenticationToken(loginId, password, loginType);
            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse login request", e);
        }
    }

    @Override
    protected void successfulAuthentication
            (HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        String loginType = userDetails.getLoginType().name();
        Long memberId = userDetails.getMemberId();
        String loginId = userDetails.getLoginId();
        String role = userDetails.getRole();

        String accessToken = jwtUtil.createToken(JwtUtil.ACCESS_TOKEN, loginType, memberId, loginId, role);
        String refreshToken = jwtUtil.createToken(JwtUtil.REFRESH_TOKEN, loginType, memberId, loginId, role);

        refreshTokenRepository.save(loginType, memberId, refreshToken, jwtUtil.getRefreshTokenTime());

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
        ResponseCookie cookie = tokenCookieProvider.getCookie(JwtUtil.REFRESH_TOKEN, refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);
        log.info("[LoginFilter] Successfully login. loginId: {}", loginId);
    }

    @Override
    protected void unsuccessfulAuthentication
            (HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
