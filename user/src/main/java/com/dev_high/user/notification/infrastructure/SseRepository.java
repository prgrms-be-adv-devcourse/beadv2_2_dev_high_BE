package com.dev_high.user.notification.infrastructure;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(String userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    public void deleteById(String userId) {
        emitters.remove(userId);
    }

    public Optional<SseEmitter> get(String userId) {
        return Optional.ofNullable(emitters.get(userId));
    }
}
