package com.aslmk.trackerservice.controller;


import com.aslmk.common.dto.TrackStreamerResponse;
import com.aslmk.common.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/streamers")
@Slf4j
public class StreamerController {

    private final TrackingService service;

    public StreamerController(TrackingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrackStreamerResponse> track(@RequestBody TrackingRequestDto request) {
        log.info("Subscribe request received: streamer='{}', provider='{}'",
                request.getStreamerUsername(),
                request.getProviderName());

        TrackStreamerResponse response = service.trackStreamer(request);

        return ResponseEntity.ok(response);
    }
}

