package com.aslmk.trackerservice;

import com.aslmk.trackerservice.domain.EventLogEntity;
import com.aslmk.trackerservice.dto.EventLogStatus;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.scheduler.PublishPendingEvents;
import com.aslmk.trackerservice.service.event.EventLogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class PublishPendingEventsUnitTests {

    @Mock
    private EventLogService service;

    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private PublishPendingEvents job;

    private static final UUID EVENT_ID_1 = UUID.randomUUID();
    private static final UUID EVENT_ID_2 = UUID.randomUUID();

    private EventLogEntity buildEntity(UUID id) {
        return EventLogEntity.builder()
                .id(id)
                .payload(Mockito.mock(StreamLifecycleEvent.class))
                .build();
    }

    @Test
    void publishPendingEvents_should_doNothing_when_noPendingEventsExist() {
        Mockito.when(service.getAllPendingEvents()).thenReturn(List.of());

        job.publishPendingEvents();

        Mockito.verify(kafkaService, Mockito.never()).send(Mockito.any());
        Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(), Mockito.any());
    }

    @Test
    void publishPendingEvents_should_updateStatusToSent_when_singleEventSentSuccessfully() {
        EventLogEntity entity = buildEntity(EVENT_ID_1);
        Mockito.when(service.getAllPendingEvents()).thenReturn(List.of(entity));
        Mockito.when(kafkaService.send((StreamLifecycleEvent) entity.getPayload()))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.publishPendingEvents();

        Mockito.verify(kafkaService).send((StreamLifecycleEvent) entity.getPayload());
        Mockito.verify(service).updateStatus(EVENT_ID_1, EventLogStatus.SENT_TO_BROKER);
    }

    @Test
    void publishPendingEvents_should_updateStatusForAllEvents_when_multipleEventsSucceed() {
        EventLogEntity entity1 = buildEntity(EVENT_ID_1);
        EventLogEntity entity2 = buildEntity(EVENT_ID_2);
        Mockito.when(service.getAllPendingEvents()).thenReturn(List.of(entity1, entity2));
        Mockito.when(kafkaService.send(Mockito.any())).thenReturn(CompletableFuture.completedFuture(null));

        job.publishPendingEvents();

        Mockito.verify(kafkaService, Mockito.times(2)).send(Mockito.any());
        Mockito.verify(service).updateStatus(EVENT_ID_1, EventLogStatus.SENT_TO_BROKER);
        Mockito.verify(service).updateStatus(EVENT_ID_2, EventLogStatus.SENT_TO_BROKER);
    }

    @Test
    void publishPendingEvents_should_continueProcessing_when_oneEventFails() {
        EventLogEntity entity1 = buildEntity(EVENT_ID_1);
        EventLogEntity entity2 = buildEntity(EVENT_ID_2);
        Mockito.when(service.getAllPendingEvents()).thenReturn(List.of(entity1, entity2));
        Mockito.when(kafkaService.send((StreamLifecycleEvent) entity1.getPayload()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")));
        Mockito.when(kafkaService.send((StreamLifecycleEvent) entity2.getPayload()))
                .thenReturn(CompletableFuture.completedFuture(null));

        Assertions.assertDoesNotThrow(() -> job.publishPendingEvents());

        Mockito.verify(service, Mockito.never()).updateStatus(Mockito.eq(EVENT_ID_1), Mockito.any());
        Mockito.verify(service).updateStatus(EVENT_ID_2, EventLogStatus.SENT_TO_BROKER);
    }
}
