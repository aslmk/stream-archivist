package com.aslmk.recordingorchestratorservice;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RecordingRequestListenerIT {

    @Value("${user.kafka.topic}")
    private String topic;

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka:latest")
    );

    @DynamicPropertySource
    static void setKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, RecordingRequestDto> kafkaTemplate;

    @MockitoBean
    private RecordingOrchestrationService service;

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        NewTopic testTopic(@Value("${user.kafka.topic}") String topic) {
            return TopicBuilder.name(topic)
                    .partitions(1).replicas(1).build();
        }
    }

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final String STREAM_QUALITY = "720p";

    @Test
    void should_receiveAndDeserializeMessageFromTopic_when_messageIsSent() {
        RecordingRequestDto dto = new RecordingRequestDto();
        dto.setStreamerUsername(STREAMER_USERNAME);
        dto.setStreamUrl(STREAM_URL);
        dto.setStreamQuality(STREAM_QUALITY);

        ArgumentCaptor<RecordingRequestDto> captor = ArgumentCaptor.forClass(RecordingRequestDto.class);

        kafkaTemplate.send(topic, dto);

        Mockito.verify(service, Mockito.timeout(5000).times(1))
                .processRecordingRequest(Mockito.any());

        Mockito.verify(service).processRecordingRequest(captor.capture());

        RecordingRequestDto actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(STREAMER_USERNAME, actual.getStreamerUsername()),
                () -> Assertions.assertEquals(STREAM_URL, actual.getStreamUrl()),
                () -> Assertions.assertEquals(STREAM_QUALITY, actual.getStreamQuality())
        );
    }
}


