package com.myce.common.config;

import com.myce.chat.config.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 설정
 * 
 * 엔드포인트: /ws/chat
 * 메시지 브로커: /topic (구독), /app (발행)
 * CORS 설정: CorsConfig와 동일한 도메인 허용
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * WebSocket 연결 엔드포인트 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("WebSocket STOMP 엔드포인트 등록: /ws/chat");
        
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(
                        "https://www.myce.live",
                        "https://myce.live", 
                        "https://media.myce.live",
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "http://localhost:8081",
                        "https://api.myce.live"
                )
                .addInterceptors(webSocketAuthInterceptor)  // JWT 인증 인터셉터
                .withSockJS();  // SockJS 폴백 지원
    }

    /**
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        log.info("WebSocket 메시지 브로커 설정");
        
        // 클라이언트 → 서버
        registry.setApplicationDestinationPrefixes("/app");
        
        // 서버 → 클라이언트 -> for 구독
        registry.enableSimpleBroker("/topic", "/queue");
    }
}