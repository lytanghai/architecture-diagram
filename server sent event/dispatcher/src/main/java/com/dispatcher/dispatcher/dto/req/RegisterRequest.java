package com.dispatcher.dispatcher.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    private String topic;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("connection_id")
    private String connectionId;

    @JsonProperty("emit_url")
    private String emitUrl;
    private int ttl;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getEmitUrl() {
        return emitUrl;
    }

    public void setEmitUrl(String emitUrl) {
        this.emitUrl = emitUrl;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
