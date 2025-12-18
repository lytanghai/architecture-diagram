package com.dispatcher.dispatcher.record;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmitRequest(
        @JsonProperty("connection_id")
        String connectionId,
        String event,
        Object payload
) {}