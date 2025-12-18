package com.dispatcher.dispatcher.registry;

import com.dispatcher.dispatcher.record.SseConnection;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InMemorySseRegistry {

    // key = userId:topic
    private final ConcurrentHashMap<String,
            ConcurrentHashMap<String, SseConnection>> store = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor();

    public InMemorySseRegistry() {
        cleaner.scheduleAtFixedRate(this::cleanup, 10, 10, TimeUnit.SECONDS);
    }

    // Equivalent to: HSET key connection_id emit_url + TTL
    public void register(
            String userId,
            String topic,
            String connectionId,
            String emitUrl,
            long ttlSeconds
    ) {
        String key = redisKey(userId, topic);

        store.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
             .put(connectionId, new SseConnection(
                     connectionId,
                     emitUrl,
                     System.currentTimeMillis() + ttlSeconds * 1000
             ));
    }

    public Map<String, Object> getAllAsJson() {

        Map<String, Object> result = new LinkedHashMap<>();

        if (store.isEmpty()) {
            result.put("message", "No connections found!");
            return result;
        }

        store.forEach((key, connections) -> {

            List<Map<String, Object>> connectionList = new ArrayList<>();

            connections.forEach((connectionId, conn) -> {

                Map<String, Object> connJson = new LinkedHashMap<>();
                connJson.put("connectionId", connectionId);
                connJson.put("emitUrl", conn.emitUrl());
                connJson.put("expiresAt", conn.expiresAt());
                connJson.put("expiresAtIso", Instant.ofEpochMilli(conn.expiresAt()).toString());

                connectionList.add(connJson);
            });

            result.put(key, connectionList);
        });

        return result;
    }


    public Map<String, SseConnection> getConnections(String userId, String topic) {
        return store.getOrDefault(redisKey(userId, topic),
                new ConcurrentHashMap<>());
    }

    public void remove(String userId, String topic, String connectionId) {
        Map<String, SseConnection> map = store.get(redisKey(userId, topic));
        if (map != null) {
            map.remove(connectionId);
            if (map.isEmpty()) {
                store.remove(redisKey(userId, topic));
            }
        }
    }

    // Equivalent to refreshing TTL (heartbeat)
    public void refreshTtl(
            String userId,
            String topic,
            String connectionId,
            long ttlSeconds
    ) {
        Map<String, SseConnection> map = store.get(redisKey(userId, topic));
        if (map != null) {
            SseConnection c = map.get(connectionId);
            if (c != null) {
                map.put(connectionId, new SseConnection(
                        c.connectionId(),
                        c.emitUrl(),
                        System.currentTimeMillis() + ttlSeconds * 1000
                ));
            }
        }
    }

    private void cleanup() {
        long now = System.currentTimeMillis();

        store.forEach((key, map) -> {
            map.values().removeIf(c -> c.expiresAt() < now);
            if (map.isEmpty()) {
                store.remove(key);
            }
        });
    }

    private String redisKey(String userId, String topic) {
        return "sse:user:" + userId + ":topic:" + topic;
    }


}
