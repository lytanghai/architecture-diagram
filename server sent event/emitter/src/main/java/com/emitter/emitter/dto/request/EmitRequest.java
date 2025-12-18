package com.emitter.emitter.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class EmitRequest {

    @JsonProperty("connection_id")
    private String connectionId;

    private String event;

    @JsonProperty("event_id")
    private String eventId;

    private Map<String,Object> payload;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
