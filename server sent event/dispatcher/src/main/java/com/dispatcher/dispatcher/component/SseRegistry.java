package com.dispatcher.dispatcher.component;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseRegistry {

    private final ConcurrentHashMap<String, SseEmitter> emitters =
            new ConcurrentHashMap<>();

    public void add(String id, SseEmitter emitter) {
        emitters.put(id, emitter);
    }

    public void remove(String id) {
        emitters.remove(id);
    }

    public Collection<SseEmitter> all() {
        return emitters.values();
    }
}
