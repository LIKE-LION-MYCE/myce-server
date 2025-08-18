package com.myce.notification.repository.impl;

import com.myce.notification.repository.EmitterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        sseEmitters.put(emitterId, sseEmitter);
        log.info("Emitters now: {}", sseEmitters.keySet());
        return sseEmitter;
    }

    public void removeSseEmitter(String memberId) {
        sseEmitters.remove(memberId);
    }

    public List<SseEmitter> findAllSseEmitterByMemberId(String memberId) {
        return sseEmitters.keySet().stream()
                .filter((key)
                        -> key.startsWith(memberId + "_"))
                .map(sseEmitters::get)
                .toList();
    }

    @Override
    public Iterable<SseEmitter> findAll() {
        return sseEmitters.values();
    }
}
