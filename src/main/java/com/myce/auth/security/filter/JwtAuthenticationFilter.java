package com.myce.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.auth.repository.TokenBlackListRepository;
import com.myce.auth.security.config.SecurityEndpoints;
import com.myce.auth.security.util.JwtUtil;
import com.myce.auth.service.AdminCodeDetailService;
import com.myce.auth.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String INVALID_TOKEN_CODE = "INVALID_TOKEN";
    private static final String EXPIRED_TOKEN_CODE = "EXPIRED_TOKEN";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AdminCodeDetailService adminCodeDetailService;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        log.debug("[JwtAuthenticationFilter] Input uri={}, method={}", uri, method);

        // jwt 존재 여부 및 유효성 검사
        if (token == null || token.isEmpty() || isPermitAll(method, uri)) {
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
        String loginTypeStr = jwtUtil.getLoginTypeFromToken(accessToken);
        LoginType loginType = LoginType.fromString(loginTypeStr);
        String loginId = jwtUtil.getLoginIdFromToken(accessToken);
        log.info("[AuthenticationFilter] JWT find userDetails by loginId: {}, loginType: {}", loginId, loginTypeStr);
        CustomUserDetails userDetails;
        try {
            userDetails = loginType.equals(LoginType.MEMBER) ?
                    (CustomUserDetails) userDetailsService.loadUserByUsername(loginId) :
                    (CustomUserDetails) adminCodeDetailService.loadCode(loginId);
        } catch (UsernameNotFoundException e) {
            log.error("[AuthenticationFilter] failed to find user details by loginId: {}", loginId, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.debug("[AuthenticationFilter] Success to find userDetail: {}", userDetails);

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

            if(tokenBlackListRepository.containsByAccessToken(token)) {
                setErrorResponse(response, INVALID_TOKEN_CODE);
                return false;
            }
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, EXPIRED_TOKEN_CODE);
            return false;
        }

        return true;
    }

    // ASYNC 디스패치는 필터 비활성화
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }


    private void setErrorResponse(HttpServletResponse response, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> body = Map.of("code", code);
        log.info("[JwtAuthenticationFilter] Set error response: {}", body);
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    private boolean isPermitAll(String method, String path) {
        if(HttpMethod.POST.name().equals(method) && pathMatcher.match("/api/payment/**", path) ||
        HttpMethod.GET.name().equals(method) && pathMatcher.match("/api/expos", path) ||
        HttpMethod.GET.name().equals(method) && pathMatcher.match("/api/expos/*/bookmark", path)) {
            return false;
        }

        if(isExist(SecurityEndpoints.ETC_PERMIT_ALL, path)) return true;

        if(HttpMethod.GET.name().equals(method)) {
            return isExist(SecurityEndpoints.GET_PERMIT_ALL, path);
        }
        if(HttpMethod.POST.name().equals(method)) {
            return isExist(SecurityEndpoints.POST_PERMIT_ALL, path);
        }
        if(HttpMethod.PATCH.name().equals(method)) {
            return isExist(SecurityEndpoints.PATCH_PERMIT_ALL, path);
        }
        if(HttpMethod.DELETE.name().equals(method)) {
            return isExist(SecurityEndpoints.DELETE_PERMIT_ALL, path);
        }

        return false;
    }

    private boolean isExist(String[] patterns, String path) {
        return Arrays.stream(patterns).anyMatch(p -> pathMatcher.match(p, path));
    }
}
