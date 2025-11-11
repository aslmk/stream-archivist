package com.aslmk.trackerservice;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.kafka.KafkaProducerConfig;
import com.aslmk.trackerservice.kafka.KafkaService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

@SpringBootTest(classes = {KafkaService.class, KafkaProducerConfig.class})
@Testcontainers
@ActiveProfiles("test")
public class KafkaServiceIntegrationTests {

    @Value("${user.kafka.topic}")
    private String topic;
    private static final String GROUP_ID = "test-group";

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("apache/kafka:latest")
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        public NewTopic testTopic(@Value("${user.kafka.topic}") String topic) {
            return TopicBuilder.name(topic)
                    .partitions(1)
                    .replicas(1)
                    .build();
        }
    }

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final String STREAM_QUALITY = "720p";

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaTemplate<String, RecordingRequestDto> kafkaTemplate;

    private Consumer<String, RecordingRequestDto> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), GROUP_ID);

        consumer = new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                new JsonDeserializer<>(RecordingRequestDto.class)
        ).createConsumer();
        consumer.subscribe(Collections.singleton(topic));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void should_readMessageFromTopic_when_kafkaServiceSendsMessageToTopic() {
        RecordingRequestDto dto = new RecordingRequestDto();
        dto.setStreamerUsername(STREAMER_USERNAME);
        dto.setStreamUrl(STREAM_URL);
        dto.setStreamQuality(STREAM_QUALITY);

        kafkaService.send(dto);

        ConsumerRecord<String, RecordingRequestDto> record = KafkaTestUtils.getSingleRecord(consumer, topic);

        Assertions.assertNotNull(record);
        RecordingRequestDto actual = record.value();
        Assertions.assertAll(
                () -> Assertions.assertEquals(dto.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(dto.getStreamUrl(), actual.getStreamUrl()),
                () -> Assertions.assertEquals(dto.getStreamQuality(), actual.getStreamQuality())
        );
    }
}
