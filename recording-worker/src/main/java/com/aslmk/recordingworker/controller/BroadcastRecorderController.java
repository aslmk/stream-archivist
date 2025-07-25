package com.aslmk.recordingworker.controller;

import com.aslmk.recordingworker.dto.RecordingRequestDto;
import com.aslmk.recordingworker.service.BroadcastRecorderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/record")
@Slf4j
public class BroadcastRecorderController {

    private final BroadcastRecorderService service;

    public BroadcastRecorderController(BroadcastRecorderService service) {
        this.service = service;
    }

    @PostMapping
    public void record(@RequestBody RecordingRequestDto request) {
        log.info("Starting to record broadcaster: {}", request.getBroadcasterUsername());
        service.recordStream(request);
    }
}
