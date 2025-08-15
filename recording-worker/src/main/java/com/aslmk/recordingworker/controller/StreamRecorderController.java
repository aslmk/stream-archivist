package com.aslmk.recordingworker.controller;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingworker.service.StreamRecorderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/record")
@Slf4j
public class StreamRecorderController {

    private final StreamRecorderService service;

    public StreamRecorderController(StreamRecorderService service) {
        this.service = service;
    }

    @PostMapping
    public void record(@RequestBody RecordingRequestDto request) {
        log.info("Starting to record streamer: {}", request.getStreamerUsername());
        service.recordStream(request);
    }
}
