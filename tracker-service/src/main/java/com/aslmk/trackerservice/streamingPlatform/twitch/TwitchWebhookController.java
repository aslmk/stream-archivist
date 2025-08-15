package com.aslmk.trackerservice.streamingPlatform.twitch;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/twitch/eventsub")
public class TwitchWebhookController {

    private final KafkaService kafkaService;

    public TwitchWebhookController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping
    public ResponseEntity<String> handleEvent(
            @RequestHeader(name = "Twitch-Eventsub-Message-Type", required = false) String messageType,
            @RequestBody TwitchEventSubRequest request) {
        if ("webhook_callback_verification".equalsIgnoreCase(messageType)) {
            return ResponseEntity.ok(request.getChallenge());
        }

        String eventType = request.getSubscription().getType();
        String login = request.getEvent().getBroadcaster_user_login();
        String id = request.getEvent().getBroadcaster_user_id();

        if ("stream.online".equals(eventType)) {
            log.info("🔥 Stream started: {} ({})", login, id);
        } else if ("stream.offline".equals(eventType)) {
            log.info("❌ Stream ended: {} ({})", login, id);
        } else {
            log.info("📦 Unknown event type: {}", eventType);
        }

        String streamUrl = "https://twitch.tv/" + login;

        RecordingRequestDto recordingRequestDto = RecordingRequestDto.builder()
                .streamerUsername(login)
                .streamUrl(streamUrl)
                .streamQuality("480p")
                .build();

        kafkaService.send(recordingRequestDto);

        return ResponseEntity.ok("ok");
    }
}
