package com.aslmk.subscriptionservice.controller;

import com.aslmk.subscriptionservice.dto.TrackedStreamerDto;
import com.aslmk.subscriptionservice.dto.TrackedStreamersResponse;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/users")
public class TrackedStreamersController {

    private final UserSubscriptionService service;

    public TrackedStreamersController(UserSubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/{userId}/streamers")
    public TrackedStreamersResponse getTrackedStreamers(@PathVariable String userId) {
        return service.getAllUserSubscriptions(userId)
                .userSubscriptions().stream()
                .map(userSub -> TrackedStreamerDto.builder()
                        .id(userSub.streamerId())
                        .build())
                .collect(Collectors
                        .collectingAndThen(Collectors.toList(), TrackedStreamersResponse::new));
    }
}
