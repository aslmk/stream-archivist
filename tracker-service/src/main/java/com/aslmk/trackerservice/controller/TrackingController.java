package com.aslmk.trackerservice.controller;

import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/streamers")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/track")
    public ResponseEntity<Void> trackStreamer(@RequestBody TrackingRequestDto trackingRequest) {
        log.info("Subscribe request received: streamer '{}', provider '{}', stream_quality '{}'",
                trackingRequest.getStreamerUsername(),
                trackingRequest.getProviderName(),
                trackingRequest.getStreamQuality());

        trackingService.trackStreamer(trackingRequest);

        log.info("Webhook subscription created: streamer='{}', provider='{}'",
                trackingRequest.getStreamerUsername(), trackingRequest.getProviderName());

        return ResponseEntity.ok().build();
    }
}
