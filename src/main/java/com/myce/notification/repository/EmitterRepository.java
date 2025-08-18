package com.myce.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface EmitterRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter);
    void removeSseEmitter(String memberId);
    List<SseEmitter> findAllSseEmitterByMemberId(String memberId);
    Iterable<SseEmitter> findAll();
}
