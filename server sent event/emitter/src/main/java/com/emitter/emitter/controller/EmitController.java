package com.emitter.emitter.controller;

import com.emitter.emitter.dto.request.EmitRequest;
import com.emitter.emitter.service.EmitterService;
import com.emitter.emitter.service.SseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/emitter")
@CrossOrigin(origins = "*") // allow all origins, or restrict to your frontend
public class EmitController {

    @Autowired
    private EmitterService emitterService;

    @GetMapping(value = "/sse/{topic}/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Object register(@PathVariable String topic, @PathVariable String userId) {
        return emitterService.register(topic, userId);
    }

    @PostMapping("/emit")
    public ResponseEntity<String> emit(@RequestBody EmitRequest request) {
        return emitterService.emit(request);
    }

    @GetMapping("/close/{connectionId}")
    public ResponseEntity<String> closeConnection(@PathVariable String connectionId) {
        SseConnection connection = SseConnection.getConnections().get(connectionId);

        if (connection != null) {
            connection.close();
            return ResponseEntity.ok("Connection closed");
        }

        return ResponseEntity.status(404).body("Connection not found");
    }


}
