package com.dispatcher.dispatcher.interfaze;

import com.dispatcher.dispatcher.dto.req.RegisterRequest;

import java.io.IOException;

public interface SseConnectionRegistry {

    void register(RegisterRequest registerRequest) throws IOException;

    void remove(String userId, String topic, String connectionId);

}
