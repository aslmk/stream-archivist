package com.aslmk.recordingorchestratorservice;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RecordingRequestListenerIntegrationTests {

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

    private Consumer<String, RecordingRequestDto> consumer;

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

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(KAFKA.getBootstrapServers(), "true");

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.aslmk.common.dto");

        consumer = new DefaultKafkaConsumerFactory<String, RecordingRequestDto>(consumerProps)
                        .createConsumer();

        consumer.subscribe(Collections.singleton(topic));
    }

    @Test
    void should_receiveAndDeserializeMessageFromTopic_when_messageIsSent() {
        RecordingRequestDto dto = new RecordingRequestDto();
        dto.setStreamerUsername(STREAMER_USERNAME);
        dto.setStreamUrl(STREAM_URL);
        dto.setStreamQuality(STREAM_QUALITY);

        kafkaTemplate.send(topic, dto);

        ConsumerRecord<String, RecordingRequestDto> record =
                KafkaTestUtils.getSingleRecord(consumer, topic, Duration.ofSeconds(30));

        RecordingRequestDto actual = record.value();

        Assertions.assertAll(
                () -> Assertions.assertEquals(dto.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(dto.getStreamUrl(), actual.getStreamUrl()),
                () -> Assertions.assertEquals(dto.getStreamQuality(), actual.getStreamQuality())
        );
    }
}


