package com.aslmk.subscriptionservice.controller;

import com.aslmk.common.constants.GatewayHeaders;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.SubscriptionRequest;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class SubscriptionController {

    private final SubscriptionOrchestrator orchestrator;

    public SubscriptionController(SubscriptionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@NotEmpty @RequestHeader(GatewayHeaders.USER_ID) String userId,
                          @NotEmpty @RequestHeader(GatewayHeaders.PROVIDER_NAME) String userProviderName,
                          @RequestBody @Valid SubscriptionRequest subscriptionRequest) {
        UserRef userRef = new UserRef(userId, userProviderName);
        StreamerRef streamerRef = new StreamerRef(subscriptionRequest.getStreamerUsername(),
                subscriptionRequest.getProviderName());

        orchestrator.subscribe(userRef, streamerRef);
    }
}
