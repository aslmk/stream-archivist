package com.aslmk.recordingorchestratorservice;

import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.rabbitmq.RabbitMqService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class RabbitMqServiceIntegrationTests {

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

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";

    @Autowired
    private RabbitMqService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void should_sendMessageToTheQueue() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);

        StreamLifecycleEvent request = StreamLifecycleEvent.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .build();

        service.sendMessage(request);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollDelay(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    QueueInformation queueInformation = rabbitAdmin.getQueueInfo(queueName);
                    Assertions.assertNotNull(queueInformation);
                    Assertions.assertEquals(1, queueInformation.getMessageCount());
                });
    }

    @Test
    void should_receiveAndDeserializeMessageFromQueue_when_messageIsSent() {
        StreamLifecycleEvent request = StreamLifecycleEvent.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .build();

        service.sendMessage(request);

        Message message = rabbitTemplate.receive(queueName, 1000);

        Assertions.assertNotNull(message);

        StreamLifecycleEvent actual =
                (StreamLifecycleEvent) rabbitTemplate.getMessageConverter()
                        .fromMessage(message);

        Assertions.assertAll(
                () -> Assertions.assertEquals(request.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(request.getStreamUrl(), actual.getStreamUrl())
        );
    }
}
