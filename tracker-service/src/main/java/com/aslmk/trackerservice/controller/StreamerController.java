package com.aslmk.trackerservice.controller;


import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(@RequestParam(name = "streamerId") String streamerId) {
        log.info("Unsubscribe request received: streamerId='{}'", streamerId);
        service.unsubscribe(streamerId);
        return ResponseEntity.noContent().build();
    }
}

