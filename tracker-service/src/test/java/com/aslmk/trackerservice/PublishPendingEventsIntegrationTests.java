package com.aslmk.trackerservice;

import com.aslmk.trackerservice.domain.EventLogEntity;
import com.aslmk.trackerservice.domain.EventType;
import com.aslmk.trackerservice.dto.EventLogStatus;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.repository.EventLogRepository;
import com.aslmk.trackerservice.scheduler.PublishPendingEvents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PublishPendingEventsIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private PublishPendingEvents job;

    @Autowired
    private EventLogRepository eventLogRepository;

    @MockitoBean
    private KafkaService kafkaService;

    @BeforeEach
    void setUp() {
        eventLogRepository.deleteAll();
    }

    private EventLogEntity buildPendingEntity() {
        return EventLogEntity.builder()
                .payload(StreamLifecycleEvent.builder().build())
                .status(EventLogStatus.PENDING.name())
                .eventType(EventType.STREAM_STARTED.name())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void publishPendingEvents_should_doNothing_when_noPendingEventsExist() {
        job.publishPendingEvents();

        Mockito.verify(kafkaService, Mockito.never()).send(Mockito.any());
        Assertions.assertEquals(0, eventLogRepository.count());
    }

    @Test
    void publishPendingEvents_should_updateStatusToSent_when_singleEventSentSuccessfully() {
        EventLogEntity entity1 = eventLogRepository.save(buildPendingEntity());
        Mockito.when(kafkaService.send(Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.publishPendingEvents();

        EventLogEntity updated = eventLogRepository.findById(entity1.getId()).orElseThrow();
        Assertions.assertEquals(EventLogStatus.SENT_TO_BROKER.name(), updated.getStatus());
    }

    @Test
    void publishPendingEvents_should_updateStatusForAllEvents_when_multipleEventsSucceed() {
        eventLogRepository.save(buildPendingEntity());
        eventLogRepository.save(buildPendingEntity());
        Mockito.when(kafkaService.send(Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.publishPendingEvents();

        eventLogRepository.findAll()
                .iterator()
                .forEachRemaining(e -> Assertions
                        .assertEquals(EventLogStatus.SENT_TO_BROKER.name(), e.getStatus()));
    }

    @Test
    void publishPendingEvents_should_continueProcessing_when_oneEventFails() {
        EventLogEntity entity1 = eventLogRepository.save(buildPendingEntity());
        EventLogEntity entity2 = eventLogRepository.save(buildPendingEntity());

        Mockito.when(kafkaService.send(Mockito.any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")))
                .thenReturn(CompletableFuture.completedFuture(null));


        Assertions.assertDoesNotThrow(() -> job.publishPendingEvents());

        EventLogEntity failedEntity = eventLogRepository.findById(entity1.getId()).orElseThrow();
        Assertions.assertEquals(EventLogStatus.PENDING.name(), failedEntity.getStatus());

        EventLogEntity succeededEntity = eventLogRepository.findById(entity2.getId()).orElseThrow();
        Assertions.assertEquals(EventLogStatus.SENT_TO_BROKER.name(), succeededEntity.getStatus());
    }
}