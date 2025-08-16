package com.myce.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 채팅 설정 - AWS Bedrock Nova Lite
 */
@Slf4j
@Configuration
public class AIConfig {

    /**
     * MYCE 플랫폼 AI 상담사용 ChatClient Bean 생성
     */
    @Bean
    public ChatClient chatClient(@Autowired(required = false) ChatClient.Builder chatClientBuilder) {
        if (chatClientBuilder == null) {
            log.error("ChatClient.Builder를 사용할 수 없습니다. AWS Bedrock 설정을 확인해주세요.");
            throw new RuntimeException("AWS Bedrock ChatClient.Builder not available");
        }

        ChatClient chatClient = chatClientBuilder
                .defaultSystem("당신은 MYCE 박람회 플랫폼의 친절한 AI 상담사 '찍찍봇'입니다.")
                .build();

        log.info("MYCE AI 상담사용 ChatClient Bean이 성공적으로 생성되었습니다.");
        return chatClient;
    }
}