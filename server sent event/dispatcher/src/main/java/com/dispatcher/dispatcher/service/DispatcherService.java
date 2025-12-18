package com.dispatcher.dispatcher.service;

import com.dispatcher.dispatcher.dto.req.DispatcherRequest;
import com.dispatcher.dispatcher.record.EmitRequest;
import com.dispatcher.dispatcher.record.SseConnection;
import com.dispatcher.dispatcher.registry.InMemorySseRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class DispatcherService {

    private static final long MAX_EVENT_TTL = 86400; // example
    private static final String PERSIST_EVENT_URL = "http://event-tracker/persist";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private InMemorySseRegistry sseRegistry;

    public ResponseEntity<?> dispatch(DispatcherRequest req) {
        String topic = req.getTopic();
        String userId = req.getUserId();
        String event = req.getEvent();
        Map<String,Object> payload = req.getPayload();

        Map<String, SseConnection> connections = sseRegistry.getConnections(userId, topic);

        if (connections.isEmpty()) {
            System.out.printf(
                    "No connections for user %s and topic %s%n",
                    userId, topic
            );
            return ResponseEntity.ok(Map.of("message", "No connections"));
        }

        String redisKey = "sse:user:" + userId + ":topic:" + topic;

        // ---- dispatch to each emit_url
        for (Map.Entry<String, SseConnection> entry : connections.entrySet()) {

            String connectionId = entry.getKey();
            SseConnection conn = entry.getValue();

            EmitRequest data = new EmitRequest(
                    connectionId,
                    event,
                    payload
            );

            System.out.println("Emitting to " + conn.emitUrl() + " " + data);

            try {
                ResponseEntity<String> emitResponse =
                        restTemplate.postForEntity(
                                conn.emitUrl(),
                                data,
                                String.class
                        );

                int status = emitResponse.getStatusCode().value();
                String text = emitResponse.getBody();

                System.out.printf(
                        "Emit response (%d): %s%n",
                        status, text
                );

                if (status != 202) {
                    System.out.println(
                            "Client may have disconnected. Removing connection..."
                    );
                    sseRegistry.remove(userId, topic, connectionId);
                }

            } catch (Exception ex) {
                // same as fetch failure in Node.js
                sseRegistry.remove(userId, topic, connectionId);
            }
        }

        return ResponseEntity.ok(
                Map.of("message",
                        "Found " + connections.size() + " connection(s)")
        );
    }


}
