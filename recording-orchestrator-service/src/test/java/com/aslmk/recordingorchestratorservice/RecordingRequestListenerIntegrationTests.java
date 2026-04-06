package com.aslmk.recordingorchestratorservice;

import com.aslmk.recordingorchestratorservice.dto.RecordingEventType;
import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleType;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.Duration;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RecordingRequestListenerIntegrationTests {

    @Value("${user.kafka.stream-lifecycle-topic}")
    private String streamLifecycleTopic;

    @Value("${user.kafka.recording-lifecycle-topic}")
    private String recordingLifecycleTopic;

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
    }

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final UUID STREAMER_ID = UUID.randomUUID();
    private static final String FILENAME = "recording-123.mp4";

    @Test
    void should_processStreamEvent_when_streamStartedEventReceived() throws Exception {
        StreamLifecycleEvent event = new StreamLifecycleEvent();
        event.setEventType(StreamLifecycleType.STREAM_STARTED);
        event.setStreamerId(STREAMER_ID);
        event.setStreamerUsername(STREAMER_USERNAME);
        event.setStreamUrl(STREAM_URL);

        kafkaTemplate.send(streamLifecycleTopic, objectMapper.writeValueAsString(event));

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.times(1))
                                .processStreamEvent(Mockito.argThat(e ->
                                        e.getEventType().equals(StreamLifecycleType.STREAM_STARTED)
                                                && e.getStreamerId().equals(STREAMER_ID)
                                                && e.getStreamerUsername().equals(STREAMER_USERNAME)
                                ))
                );
    }

    @Test
    void should_ignoreStreamEvent_when_eventTypeIsNotStreamStarted() throws Exception {
        StreamLifecycleEvent event = new StreamLifecycleEvent();
        event.setEventType(StreamLifecycleType.STREAM_ENDED);
        event.setStreamerId(STREAMER_ID);
        event.setStreamerUsername(STREAMER_USERNAME);

        kafkaTemplate.send(streamLifecycleTopic, objectMapper.writeValueAsString(event));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(3))
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.never())
                                .processStreamEvent(Mockito.any())
                );
    }

    @Test
    void should_processRecordingEvent_when_recordingFinishedEventReceived() throws Exception {
        RecordingStatusEvent event = new RecordingStatusEvent();
        event.setEventType(RecordingEventType.RECORDING_FINISHED);
        event.setStreamerId(STREAMER_ID);
        event.setFilename(FILENAME);

        kafkaTemplate.send(recordingLifecycleTopic, objectMapper.writeValueAsString(event));

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.times(1))
                                .processRecordingEvent(Mockito.argThat(e ->
                                        e.getEventType().equals(RecordingEventType.RECORDING_FINISHED)
                                                && e.getStreamerId().equals(STREAMER_ID)
                                                && e.getFilename().equals(FILENAME)
                                ))
                );
    }

    @Test
    void should_ignoreRecordingEvent_when_eventTypeIsNotRecordingFinished() throws Exception {
        RecordingStatusEvent event = new RecordingStatusEvent();
        event.setEventType(RecordingEventType.RECORDING_STARTED);
        event.setStreamerId(STREAMER_ID);
        event.setFilename(FILENAME);

        kafkaTemplate.send(recordingLifecycleTopic, objectMapper.writeValueAsString(event));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofSeconds(3))
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.never())
                                .processRecordingEvent(Mockito.any())
                );
    }
}