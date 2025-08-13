package com.myce.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 테스트 컨트롤러 - AWS Bedrock Nova 연동 확인
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/test")
@RequiredArgsConstructor
public class AITestController {

    private ChatClient chatClient;

    @Autowired(required = false)
    public AITestController(ChatClient.Builder chatClientBuilder) {
        if (chatClientBuilder != null) {
            this.chatClient = chatClientBuilder
                    .defaultSystem("You are a helpful MYCE exhibition assistant. " +
                                  "Respond in Korean for Korean questions, English for English questions.")
                    .build();
            log.info("ChatClient successfully initialized with Nova Lite");
        } else {
            log.warn("ChatClient.Builder not available - check AWS Bedrock configuration");
        }
    }

    /**
     * AWS Bedrock 연결 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            if (chatClient == null) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "ChatClient not initialized",
                    "solution", "Check AWS credentials and Bedrock model access"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "ready",
                "message", "AWS Bedrock Nova Lite is configured",
                "model", "amazon.nova-lite-v1:0"
            ));

        } catch (Exception e) {
            log.error("Status check failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "solution", "Check AWS Console → Bedrock → Model Access"
            ));
        }
    }

    /**
     * 간단한 AI 응답 테스트
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> testChat(@RequestBody Map<String, String> request) {
        try {
            if (chatClient == null) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "AI not available",
                    "response", "Please check AWS Bedrock setup"
                ));
            }

            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                message = "안녕하세요! MYCE 플랫폼에 대해 알려주세요.";
            }

            log.info("Testing AI with message: {}", message);
            
            String response = chatClient
                .prompt(message)
                .call()
                .content();

            log.info("AI Response received: {}", response);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "userMessage", message,
                "aiResponse", response,
                "model", "amazon.nova-lite-v1:0"
            ));

        } catch (Exception e) {
            log.error("AI chat test failed", e);
            
            String errorMessage = e.getMessage();
            String solution = "Check AWS Bedrock model access and IAM permissions";
            
            if (errorMessage.contains("AccessDeniedException")) {
                solution = "Enable 'Amazon Nova Lite' in AWS Console → Bedrock → Model Access";
            } else if (errorMessage.contains("ValidationException")) {
                solution = "Check if Nova Lite model is available in ap-northeast-2 region";
            }

            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", errorMessage,
                "solution", solution
            ));
        }
    }

    /**
     * 한국어 응답 테스트
     */
    @GetMapping("/korean-test")
    public ResponseEntity<Map<String, Object>> testKorean() {
        return testChat(Map.of("message", "안녕하세요! MYCE 박람회 플랫폼에 대해 간단히 설명해주세요."));
    }

    /**
     * 영어 응답 테스트  
     */
    @GetMapping("/english-test")
    public ResponseEntity<Map<String, Object>> testEnglish() {
        return testChat(Map.of("message", "Hello! Can you briefly explain about MYCE exhibition platform?"));
    }
}