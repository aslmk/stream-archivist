package com.aslmk.trackerservice.controller;

import com.aslmk.trackerservice.dto.AddBroadcasterRequestDto;
import com.aslmk.trackerservice.dto.DeleteBroadcasterRequestDto;
import com.aslmk.trackerservice.service.BroadcasterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/broadcaster")
public class BroadcasterController {
    private final BroadcasterService broadcasterService;

    public BroadcasterController(BroadcasterService broadcasterService) {
        this.broadcasterService = broadcasterService;
    }

    @PostMapping
    public ResponseEntity<Void> addBroadcaster(@RequestParam AddBroadcasterRequestDto newBroadcaster) {
        broadcasterService.saveBroadcaster(newBroadcaster);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBroadcaster(@RequestParam DeleteBroadcasterRequestDto deleteBroadcaster) {
        broadcasterService.deleteBroadcaster(deleteBroadcaster);
        return ResponseEntity.noContent().build();
    }
}
