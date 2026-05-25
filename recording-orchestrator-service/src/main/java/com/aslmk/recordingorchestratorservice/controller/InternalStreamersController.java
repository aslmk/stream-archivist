package com.aslmk.recordingorchestratorservice.controller;

import com.aslmk.recordingorchestratorservice.dto.StreamListResponse;
import com.aslmk.recordingorchestratorservice.service.StreamSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/streamers")
public class InternalStreamersController {
    private final StreamSessionService service;

    public InternalStreamersController(StreamSessionService service) {
        this.service = service;
    }

    @GetMapping("/{streamerId}/streams")
    public StreamListResponse findStreamIdsByStreamerId(@PathVariable UUID streamerId) {
        return service.findStreamIdsByStreamerId(streamerId);
    }
}
