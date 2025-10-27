package com.aslmk.trackerservice.streamingPlatform.twitch;

import com.aslmk.trackerservice.service.TwitchEventHandlerService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
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
            @RequestBody TwitchEventSubRequest request) {

        if ("webhook_callback_verification".equalsIgnoreCase(messageType)) {
            return ResponseEntity.ok(request.getChallenge());
        }

        handler.handle(request);

        return ResponseEntity.ok("ok");
    }
}
