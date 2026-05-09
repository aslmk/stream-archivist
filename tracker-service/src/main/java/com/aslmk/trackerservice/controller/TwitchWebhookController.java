package com.aslmk.trackerservice.controller;

import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.service.event.TwitchEventHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/twitch/eventsub")
public class TwitchWebhookController {

    private final TwitchEventHandlerService handler;
    public TwitchWebhookController(TwitchEventHandlerService handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<String> handleEvent(
            @RequestHeader(name = "Twitch-Eventsub-Message-Type", required = false) String messageType,
            @RequestHeader(name = "Twitch-Eventsub-Message-Id", required = false) String eventId,
            @RequestBody TwitchEventSubRequest request) {

        if ("webhook_callback_verification".equalsIgnoreCase(messageType)) {
            log.debug("Responding to Twitch challenge verification for subscriptionType='{}'",
                    request.getSubscription().getType());
            return ResponseEntity.ok(request.getChallenge());
        }

        handler.handle(request, eventId);
        return ResponseEntity.noContent().build();
    }
}
