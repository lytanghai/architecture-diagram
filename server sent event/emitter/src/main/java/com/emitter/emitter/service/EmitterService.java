package com.emitter.emitter.service;

import com.emitter.emitter.dto.request.EmitRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class EmitterService {

    private final ExecutorService emitExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private int connectionTtl = 6000000;
    private int connectionKeepAliveInterval = 30;
    private int connectionRetryDelay = 5;
    private String hostname = "emitter-pod-3";

    @Value("${emitter.emit.url}")
    private String emitUrl;

    @Value("${dispatcher.register.url}")
    private String registerUrl;

    public Object register(String topic, String userId) {

        String connectionId = UUID.randomUUID().toString();
        SseConnection connection = new SseConnection(connectionId, userId, topic, connectionTtl);

        connection.setEmitUrl(emitUrl);
        connection.setRegistrationUrl(registerUrl);

        SseEmitter emitter = connection.register();

        connection.createRegisterSchedule(scheduledExecutor);
        connection.createKeepAliveSchedule(
                scheduledExecutor,
                connectionKeepAliveInterval
        );

        // send init event AFTER emitter is returned
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("connection_id", connectionId);
            event.put("time", Instant.now().toString());
            event.put("origin", hostname);
            event.put("topic", topic);
            event.put("user_id", userId);

            emitter.send(
                    SseEmitter.event()
                            .name("init")
                            .data(event)
                            .reconnectTime(connectionRetryDelay * 1000L)
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public ResponseEntity<String> emit(@RequestBody EmitRequest request) {
        String connectionId = request.getConnectionId();

        SseConnection connection = SseConnection.getConnections().get(connectionId);

        if (connection == null) {
            return ResponseEntity.status(404).body("Connection not found.");
        }

        emitExecutor.execute(() -> {
            connection.emit(
                    SseEmitter.event()
                            .name(request.getEvent())
                            .id(request.getEventId())
                            .data(request.getPayload())
                            .reconnectTime(connectionRetryDelay * 1000L));
        });

        return ResponseEntity.status(202).body("Accepted");
    }

}
