package com.aslmk.trackerservice.scheduler;

import com.aslmk.trackerservice.domain.EventLogEntity;
import com.aslmk.trackerservice.dto.EventLogStatus;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.event.EventLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class PublishPendingEvents {

    private final EventLogService service;
    private final KafkaService kafkaService;

    public PublishPendingEvents(EventLogService service, KafkaService kafkaService) {
        this.service = service;
        this.kafkaService = kafkaService;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void publishPendingEvents() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<EventLogEntity> successfullEvents = new ArrayList<>();

        List<EventLogEntity> pendingEvents = service.getAllPendingEvents();

        log.debug("Publishing pending events: count='{}'", pendingEvents.size());

        for (EventLogEntity pendingEvent: pendingEvents) {
            CompletableFuture<Void> future = kafkaService
                    .send((StreamLifecycleEvent) pendingEvent.getPayload())
                    .thenAccept(result -> successfullEvents.add(pendingEvent))
                    .exceptionally(ex -> {
                        log.warn("Failed to send event '{}' to Kafka: {}",
                                pendingEvent.getId(), ex.getMessage());
                        return null;
                    });

            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.get(5, TimeUnit.SECONDS);

            if (!successfullEvents.isEmpty()) {
                for (EventLogEntity sentEvents: successfullEvents) {
                    service.updateStatus(sentEvents.getId(), EventLogStatus.SENT_TO_BROKER);
                }
            }


        } catch (TimeoutException e) {
            if (!successfullEvents.isEmpty()) {
                for (EventLogEntity sentEvents: successfullEvents) {
                    service.updateStatus(sentEvents.getId(), EventLogStatus.SENT_TO_BROKER);
                }
            }

            log.error("Batch timed out. Processed '{}' events out of '{}'",
                    successfullEvents.size(), pendingEvents.size());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unexpected error waiting for Kafka batch", e);
        }
    }
}
