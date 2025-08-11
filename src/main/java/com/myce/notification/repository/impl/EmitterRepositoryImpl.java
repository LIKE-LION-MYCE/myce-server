package com.myce.notification.repository.impl;

import com.myce.notification.repository.EmitterRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        sseEmitters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    public void removeSseEmitter(String memberId) {
        sseEmitters.remove(memberId);
    }

    public List<SseEmitter> findAllSseEmitterByMemberId(String memberId) {
        System.out.println("sseEmitters.keySet(): " + sseEmitters.keySet());
        return sseEmitters.keySet().stream()
                .filter((key)
                        -> key.startsWith(memberId + "_"))
                .map(sseEmitters::get)
                .toList();
    }
}
