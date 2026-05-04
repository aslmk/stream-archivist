package com.aslmk.recordingorchestratorservice;

import com.aslmk.recordingorchestratorservice.dto.*;
import com.aslmk.recordingorchestratorservice.repository.RecordedFilePartRepository;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class RecordingRequestListenerIntegrationTests {

    @Value("${user.kafka.stream-lifecycle-topic}")
    private String streamLifecycleTopic;

    @Value("${user.kafka.recording-lifecycle-topic}")
    private String recordingLifecycleTopic;

    @Value("${user.kafka.recording-part-lifecycle-topic}")
    private String recordingPartLifecycleTopic;

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka:latest")
    );

    @DynamicPropertySource
    static void setKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordedFilePartRepository recordedFilePartRepository;

    @MockitoBean
    private RecordingOrchestrationService service;

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        NewTopic streamLifecycleTestTopic(@Value("${user.kafka.stream-lifecycle-topic}") String topic) {
            return TopicBuilder.name(topic)
                    .partitions(1).replicas(1).build();
        }

        @Bean
        NewTopic recordingLifecycleTestTopic(@Value("${user.kafka.recording-lifecycle-topic}") String topic) {
            return TopicBuilder.name(topic)
                    .partitions(1).replicas(1).build();
        }

        @Bean
        NewTopic recordingPartLifecycleTestTopic(@Value("${user.kafka.recording-part-lifecycle-topic}") String topic) {
            return TopicBuilder.name(topic)
                    .partitions(1).replicas(1).build();
        }
    }

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final String FILENAME = "recording-123.mp4";

    @Test
    void should_processStreamEvent_when_streamStartedEventReceived() throws Exception {
        StreamLifecycleEvent event = new StreamLifecycleEvent();
        event.setEventType(StreamLifecycleType.STREAM_STARTED);
        event.setStreamerId(STREAM_ID);
        event.setStreamerUsername(STREAMER_USERNAME);
        event.setStreamUrl(STREAM_URL);

        kafkaTemplate.send(streamLifecycleTopic,
                objectMapper.writeValueAsString(event))
                .get(5, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.atLeastOnce())
                                .processStreamEvent(Mockito.argThat(e ->
                                        e.getEventType().equals(StreamLifecycleType.STREAM_STARTED)
                                                && e.getStreamerId().equals(STREAM_ID)
                                                && e.getStreamerUsername().equals(STREAMER_USERNAME)
                                ))
                );
    }

    @Test
    void should_processRecordingEvent_when_recordingFinishedEventReceived() throws Exception {
        RecordingStatusEvent event = new RecordingStatusEvent();
        event.setEventType(RecordingEventType.RECORDING_FINISHED);
        event.setStreamId(STREAM_ID);
        event.setFilename(FILENAME);

        kafkaTemplate.send(recordingLifecycleTopic,
                objectMapper.writeValueAsString(event))
                .get(5, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.atLeastOnce())
                                .processRecordingEvent(Mockito.argThat(e ->
                                        e.getEventType().equals(RecordingEventType.RECORDING_FINISHED)
                                                && e.getStreamId().equals(STREAM_ID)
                                                && e.getFilename().equals(FILENAME)
                                ))
                );
    }

    @Test
    void should_processRecordingPartEvent() throws Exception {
        int partIndex = 0;

        RecordedPartEvent event = RecordedPartEvent.builder()
                .eventType(RecordedPartEventType.PART_RECORDED)
                .streamId(STREAM_ID)
                .filePartName("stream_name.ts")
                .partIndex(partIndex)
                .filePartPath("/tmp/stream_name.ts")
                .build();

        kafkaTemplate.send(recordingPartLifecycleTopic,
                objectMapper.writeValueAsString(event))
                .get(5, TimeUnit.SECONDS);

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> Mockito.verify(service, Mockito.atLeastOnce())
                        .processRecordingPartEvent(Mockito.argThat(e ->
                    e.getEventType().equals(RecordedPartEventType.PART_RECORDED) &&
                            e.getStreamId().equals(STREAM_ID) &&
                            e.getPartIndex() == partIndex
                )));
    }
}