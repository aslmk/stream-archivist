package com.aslmk.trackerservice.controller;


import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.subscription.TrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/streamers")
public class StreamerController {

    private final TrackingService service;

    public StreamerController(TrackingService service) {
        this.service = service;
    }

    @PostMapping
    public TrackStreamerResponse track(@RequestBody TrackingRequestDto request) {
        return service.trackStreamer(request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@RequestParam(name = "streamerId") String streamerId) {
        service.unsubscribe(streamerId);
    }
}

