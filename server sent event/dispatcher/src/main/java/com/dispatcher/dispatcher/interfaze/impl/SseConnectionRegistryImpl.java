package com.dispatcher.dispatcher.interfaze.impl;

import com.dispatcher.dispatcher.dto.req.RegisterRequest;
import com.dispatcher.dispatcher.interfaze.SseConnectionRegistry;
import com.dispatcher.dispatcher.registry.InMemorySseRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SseConnectionRegistryImpl implements SseConnectionRegistry {

    private static final String REGISTER_EVENT = "sse.user.connected";

    @Autowired
    private InMemorySseRegistry sseRegistry;

    @Override
    public SseEmitter register(String userId,
                               String topic,
                               String emitUrl,
                               String connectionId,
                               Long ttl) throws IOException {


        if(connectionId == null || connectionId.isEmpty()) {
            connectionId = UUID.randomUUID().toString();
        }

        // IMPORTANT: no timeout
        SseEmitter emitter = new SseEmitter(0L);

        sseRegistry.register(
                userId,
                topic,
                connectionId,
                emitUrl,
                6000
        );

        String finalUserId = userId;
        String finalTopic = topic;
        String finalConnectionId = connectionId;
        emitter.onCompletion(() ->
                sseRegistry.remove(finalUserId, finalTopic, finalConnectionId)
        );

        emitter.send(SseEmitter.event()
                .name(REGISTER_EVENT)
                .id(connectionId)
                .data("user connected")
        );

        return emitter;
    }

    @Override
    public void register(RegisterRequest registerRequest) throws IOException {
        String connectionId = registerRequest.getConnectionId();

        if(connectionId == null || connectionId.isEmpty()) {
            connectionId = UUID.randomUUID().toString();
        }

        String topic = registerRequest.getTopic();
        String userId = registerRequest.getUserId();
        String emitUrl = registerRequest.getEmitUrl();

        // IMPORTANT: no timeout
        SseEmitter emitter = new SseEmitter(0L);

        sseRegistry.register(
                userId,
                topic,
                connectionId,
                emitUrl,
                6000
        );

        String finalUserId = userId;
        String finalTopic = topic;
        String finalConnectionId = connectionId;
        emitter.onCompletion(() ->
                sseRegistry.remove(finalUserId, finalTopic, finalConnectionId)
        );

        emitter.send(SseEmitter.event()
                .name(REGISTER_EVENT)
                .id(connectionId)
                .data("user connected")
        );

        SseEmitter.SseEventBuilder eventBuilder =
                SseEmitter.event()
                        .name(REGISTER_EVENT)
                        .id(connectionId)
                        .data("some data");

        emitter.send(eventBuilder);
    }

    @Override
    public Optional<SseEmitter> get(String connectionId) {
        return Optional.empty();
    }

    @Override
    public void remove(String userId, String topic, String connectionId) {
        sseRegistry.remove(userId, topic, connectionId);
    }
    public String buildObject(String userid, String topic) {
        return "sse:user:" + userid + ":topic:" + topic;
    }

    @Override
    public Collection<SseEmitter> getAll() {
        return List.of();
    }

    @Override
    public void refreshTtl(String connectionId, long ttlMillis) {}

}
