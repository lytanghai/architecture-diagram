package com.emitter.emitter.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class SseConnection {
    private final Logger logger = LoggerFactory.getLogger(SseConnection.class);

    private static final ConcurrentHashMap<String, SseConnection> connections = new ConcurrentHashMap<>();

    @Setter
    private String registrationUrl;

    @Setter
    private String emitUrl;

    private final String connectionId;
    private final String userId;
    private final String topic;
    private final int ttl;

    private SseEmitter emitter;
    private ScheduledFuture<?> keepAliveSchedule;
    private ScheduledFuture<?> registerSchedule;

    public SseEmitter register() {
        callDispatcherRegister();

        connections.put(connectionId, this);

        emitter = new SseEmitter(0L); // no timeout
        emitter.onCompletion(this::close);
        emitter.onTimeout(this::close);
        emitter.onError(err -> this.close());

        return emitter;
    }

    private void callDispatcherRegister() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("topic", topic);
        payload.put("user_id", userId);
        payload.put("connection_id", connectionId);
        payload.put("emit_url", emitUrl);
        payload.put("ttl", ttl);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(registrationUrl, request, String.class);

        logger.info("Registered connection. Response: {}", response.getBody());
    }

    public void createKeepAliveSchedule(ScheduledExecutorService scheduledExecutor, int keepAliveInterval) {
        if (keepAliveSchedule != null) {
            keepAliveSchedule.cancel(false);
        }

        keepAliveSchedule = scheduledExecutor.scheduleAtFixedRate(this::sendKeepAlive, keepAliveInterval,
                keepAliveInterval, TimeUnit.SECONDS);
    }

    public void createRegisterSchedule(ScheduledExecutorService scheduledExecutor) {
        if (registerSchedule != null) {
            registerSchedule.cancel(false);
        }

        int registerInterval = ttl - 60;
        registerSchedule = scheduledExecutor.scheduleAtFixedRate(this::reregister, registerInterval, registerInterval,
                TimeUnit.SECONDS);
    }

    private void reregister() {
        logger.info("Re-registering connection {}", connectionId);

        try {
            callDispatcherRegister();
        } catch (Exception e) {
            logger.error("Failed to re-register connection. Closing the connection. {}", e.toString());
            close();
        }
    }

    private void sendKeepAlive() {
        logger.info("Sending keep-alive to connection {}", connectionId);

        try {
            emitter.send(SseEmitter.event().comment("keepalive - " + Instant.now()));
        } catch (IOException e) {
            logger.warn("Connection {} closed during keep-alive", connectionId);
            close();
        }
    }

    public boolean emit(SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException exception) {
            logger.warn("Connection closed while emitting {}", connectionId);
            close();
            return false;
        } catch (Exception e) {
            logger.error("Something went wrong while emitting the event to connection {}: {}", connectionId,
                    e.toString());
            close();
            return false;
        }

        logger.info("Emitted to connection {}", connectionId);

        return true;
    }

    public void close() {
        emitter.complete();

        if (keepAliveSchedule != null) {
            logger.info("Canceling keep-alive schedule for connection {}", connectionId);
            keepAliveSchedule.cancel(false);
        }

        if (registerSchedule != null) {
            logger.info("Canceling register schedule for connection {}", connectionId);
            registerSchedule.cancel(false);
        }

        connections.remove(connectionId); // client disconnected
    }

    public static Map<String, SseConnection> getConnections() {
        return connections;
    }

}