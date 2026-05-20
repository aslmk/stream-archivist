package com.aslmk.recordingorchestratorservice.controller;

import com.aslmk.recordingorchestratorservice.dto.StreamStatusDto;
import com.aslmk.recordingorchestratorservice.service.StreamSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/streams")
public class InternalStreamStatusController {
    private final StreamSessionService service;

    public InternalStreamStatusController(StreamSessionService service) {
        this.service = service;
    }

    @PatchMapping("/{streamId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleStreamStatus(@PathVariable UUID streamId,
                                   @RequestBody StreamStatusDto dto) {
        service.updateStatus(streamId, dto.status());
    }
}
