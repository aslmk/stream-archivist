package com.aslmk.trackerservice.controller;

import com.aslmk.trackerservice.dto.AddStreamerRequestDto;
import com.aslmk.trackerservice.dto.DeleteStreamerRequestDto;
import com.aslmk.trackerservice.service.StreamerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/streamer")
public class StreamerController {
    private final StreamerService streamerService;

    public StreamerController(StreamerService streamerService) {
        this.streamerService = streamerService;
    }

    @PostMapping
    public ResponseEntity<Void> addStreamer(@RequestParam AddStreamerRequestDto newStreamer) {
        streamerService.saveStreamer(newStreamer);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBroadcaster(@RequestParam DeleteStreamerRequestDto deleteStreamer) {
        streamerService.deleteStreamer(deleteStreamer);
        return ResponseEntity.noContent().build();
    }
}
