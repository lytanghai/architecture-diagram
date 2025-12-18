package com.dispatcher.dispatcher.interfaze;

import com.dispatcher.dispatcher.dto.req.RegisterRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public interface SseConnectionRegistry {

    SseEmitter register(String userId,
                        String topic,
                        String emitUrl,
                        String connectionId,
                        Long ttl) throws IOException;

    void register(RegisterRequest registerRequest) throws IOException;

    Optional<SseEmitter> get(String connectionId);

    void remove(String userId, String topic, String connectionId);

    Collection<SseEmitter> getAll();

    void refreshTtl(String connectionId, long ttlMillis);
}
