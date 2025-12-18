package com.emitter.emitter.controller;

import com.emitter.emitter.dto.request.EmitRequest;
import com.emitter.emitter.service.EmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
