package com.aslmk.trackerservice.controller;


import com.aslmk.trackerservice.dto.TrackStreamerResponse;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.service.subscription.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public TrackStreamerResponse track(@RequestBody TrackingRequestDto request) {
        log.info("Subscribe request received: streamer='{}', provider='{}'",
                request.getStreamerUsername(),
                request.getProviderName());

        return service.trackStreamer(request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@RequestParam(name = "streamerId") String streamerId) {
        log.info("Unsubscribe request received: streamerId='{}'", streamerId);
        service.unsubscribe(streamerId);
    }
}

