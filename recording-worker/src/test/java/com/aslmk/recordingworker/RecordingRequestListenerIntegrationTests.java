package com.aslmk.recordingworker;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingworker.service.StreamRecorderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class RecordingRequestListenerIntegrationTests {

    @Value("${user.rabbitmq.queue.name}")
    private String queueName;


    @TestConfiguration
    static class TestRabbitMqConfig {
        @Value("${user.rabbitmq.queue.name}")
        private String queueName;

        @Bean
        public Queue testQueue() {
            return new Queue(queueName, true);
        }
    }

    @Container
    static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:4.0-management"));

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @MockitoBean
    private StreamRecorderService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final String STREAM_QUALITY = "720p";

    @Test
    void handleRecordingRequest_should_callRecordStream_when_requestIsValid() {
        RecordingRequestDto dto = buildRecordingRequestDto();

        rabbitTemplate.convertAndSend(queueName, dto);

        ArgumentCaptor<RecordingRequestDto> captor = ArgumentCaptor.forClass(RecordingRequestDto.class);

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.times(1))
                                .recordStream(captor.capture())
                );

        RecordingRequestDto actual = captor.getValue();

        Assertions.assertEquals(STREAMER_USERNAME, actual.getStreamerUsername());
        Assertions.assertEquals(STREAM_URL, actual.getStreamUrl());
        Assertions.assertEquals(STREAM_QUALITY, actual.getStreamQuality());
    }

    @Test
    void handleRecordingRequest_shouldNotCallRecordStream_when_requestIsInvalid() {

        rabbitTemplate.convertAndSend(queueName, "Invalid-json-data");

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(service, Mockito.never())
                                .recordStream(Mockito.any())
                );
    }





    private RecordingRequestDto buildRecordingRequestDto() {
        return RecordingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamQuality(STREAM_QUALITY)
                .streamUrl(STREAM_URL)
                .build();
    }
}
