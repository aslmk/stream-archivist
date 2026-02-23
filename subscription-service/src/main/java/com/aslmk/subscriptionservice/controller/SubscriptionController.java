package com.aslmk.subscriptionservice.controller;

import com.aslmk.common.constants.GatewayHeaders;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.SubscriptionRequest;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@Validated
public class SubscriptionController {

    private final SubscriptionOrchestrator orchestrator;
    private final UserSubscriptionService service;

    public SubscriptionController(SubscriptionOrchestrator orchestrator, UserSubscriptionService service) {
        this.orchestrator = orchestrator;
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@NotEmpty @RequestHeader(GatewayHeaders.USER_ID) String userId,
                          @RequestBody @Valid SubscriptionRequest subscriptionRequest) {
        UserRef userRef = new UserRef(userId);
        StreamerRef streamerRef = new StreamerRef(subscriptionRequest.getStreamerUsername(),
                subscriptionRequest.getProviderName());

        orchestrator.subscribe(userRef, streamerRef);
    }

    @GetMapping
    public ResponseEntity<UserSubscriptionsResponse> getUserSubscriptions(
            @NotEmpty @RequestHeader(GatewayHeaders.USER_ID) String userId) {

        UserSubscriptionsResponse response = service.getAllUserSubscriptions(userId);

        return ResponseEntity.ok(response);
    }
}
