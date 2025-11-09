package com.aslmk.trackerservice.controller;

import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/streamers")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/track")
    public ResponseEntity<Void> trackStreamer(@RequestBody TrackingRequestDto trackingRequest) {
        trackingService.trackStreamer(trackingRequest);
        return ResponseEntity.ok().build();
    }
}
