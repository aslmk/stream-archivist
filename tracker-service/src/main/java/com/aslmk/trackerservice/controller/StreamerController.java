package com.aslmk.trackerservice.controller;


import com.aslmk.common.dto.EntityIdResolveResponse;
import com.aslmk.trackerservice.service.StreamerResolutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/streamers")
public class StreamerController {

    private final StreamerResolutionService service;

    public StreamerController(StreamerResolutionService service) {
        this.service = service;
    }

    @GetMapping("/resolve")
    public EntityIdResolveResponse resolve(@RequestParam String providerUserId,
                                           @RequestParam String providerName) {
        UUID streamerId = service.resolveStreamerId(providerUserId, providerName);
        return EntityIdResolveResponse.builder()
                .entityId(streamerId)
                .build();
    }
}

