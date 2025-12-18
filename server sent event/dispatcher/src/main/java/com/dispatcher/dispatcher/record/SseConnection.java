package com.dispatcher.dispatcher.record;

public record SseConnection(
        String connectionId,
        String emitUrl,
        long expiresAt
) {}