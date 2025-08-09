package com.myce.chat.config;

import com.myce.auth.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 연결 시 JWT 토큰 검증 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) throws Exception {

        log.debug("WebSocket 연결 시도 - URI: {}", request.getURI());

        try {
            // HTTP 요청에서 토큰 추출
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // 토큰 유효성 검증 (사용자 정보는 추출하지 않음)
                if (jwtUtil.validateToken(token)) {
                    log.debug("WebSocket 연결 승인 - 유효한 토큰");
                    attributes.put("validated", true);
                    return true;
                }
            }

            log.debug("WebSocket 연결 허용 - 토큰 없음 (AUTH 메시지에서 처리 예정)");
            attributes.put("validated", false);

            return true; // 토큰 없어도 연결 허용 (AUTH 메시지에서 검증)  -> 2차검증

        } catch (Exception e) {
            log.error("WebSocket 연결 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        if (exception == null) {
            log.debug("WebSocket 연결 성공 - URI: {}", request.getURI());
        } else {
            log.error("WebSocket 연결 실패: {}", exception.getMessage());
        }
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest =
                    ((ServletServerHttpRequest) request).getServletRequest();

            // Authorization 헤더에서 토큰 추출
            String authHeader = servletRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            // 쿼리 파라미터에서 토큰 추출 (WebSocket 연결 시 사용)
            String tokenParam = servletRequest.getParameter("token");
            if (tokenParam != null) {
                return tokenParam;
            }
        }

        return null;
    }
}