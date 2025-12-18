package com.dispatcher.dispatcher.controller;

import com.dispatcher.dispatcher.dto.req.DispatcherRequest;
import com.dispatcher.dispatcher.dto.req.RegisterRequest;
import com.dispatcher.dispatcher.interfaze.SseConnectionRegistry;
import com.dispatcher.dispatcher.registry.InMemorySseRegistry;
import com.dispatcher.dispatcher.service.DispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/dispatcher")
@CrossOrigin(origins = "*")  // allow all origins
public class AppController {

    @Autowired
    private InMemorySseRegistry sseRegistry;

    @Autowired
    private SseConnectionRegistry sseConnectionRegistry;

    @Autowired
    private DispatcherService dispatcherService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) throws IOException {
        sseConnectionRegistry.register(registerRequest);

        return ResponseEntity.ok("registered");
    }

    @GetMapping("/view-registry")
    public ResponseEntity<Map<String, Object>> view() {
        return ResponseEntity.ok(sseRegistry.getAllAsJson());
    }

    @PostMapping("/dispatch")
    public ResponseEntity<?> dispatch(@RequestBody DispatcherRequest req) {
        return dispatcherService.dispatch(req);
    }

    @GetMapping("/close/{userId}/{topicId}/{connectionId}")
    public ResponseEntity<String> close(@PathVariable String userId, @PathVariable String topicId, @PathVariable String connectionId) {
        System.out.println("closing connection info:  " + userId + " | " + topicId+ " | " + connectionId);
        sseConnectionRegistry.remove(userId, topicId, connectionId);
        sseRegistry.remove(userId, topicId, connectionId);
        return ResponseEntity.ok("closed");
    }

}
