package com.aslmk.subscriptionservice.controller;

import com.aslmk.subscriptionservice.dto.TrackedStreamerDto;
import com.aslmk.subscriptionservice.dto.TrackedStreamersResponse;
import com.aslmk.subscriptionservice.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
public class TrackedStreamersController {

    private final SubscriptionService subscriptionService;

    public TrackedStreamersController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/{userId}/streamers")
    public TrackedStreamersResponse getTrackedStreamers(@PathVariable String userId) {
        List<TrackedStreamerDto> trackedStreamers = subscriptionService.getAllTrackedStreamers(userId);

        return TrackedStreamersResponse.builder()
                .streamers(trackedStreamers)
                .build();
    }
}
