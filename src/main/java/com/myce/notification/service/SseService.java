package com.myce.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter subscribe(Long memberId);
    void notifyToExpoClient(Long expoId, String content);
    void notifyMemberViaSseEmitters(Long memberId, String content);
}
