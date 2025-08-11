package com.myce.notification.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
    private final SseService sseService;

    @GetMapping(value = "/api/sse/subscribe", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> subscribe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long memberId = customUserDetails.getMemberId();

        log.trace("initialize Sse Connection for MemberId:" + memberId);

        return ResponseEntity.ok(sseService.subscribe(memberId));
    }

}
