package com.aslmk.recordingorchestratorservice;

import com.aslmk.recordingorchestratorservice.dto.RecordStreamJob;
import com.aslmk.recordingorchestratorservice.dto.UploadStreamRecordJob;
import com.aslmk.recordingorchestratorservice.messaging.rabbitmq.RabbitMqService;
import com.aslmk.recordingorchestratorservice.repository.JobLogRepository;
import com.aslmk.recordingorchestratorservice.repository.RecordedFilePartRepository;
import com.aslmk.recordingorchestratorservice.repository.StreamSessionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
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
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class RabbitMqServiceIntegrationTests {

    @TestConfiguration
    static class TestRabbitMqConfig {

        @Value("${user.rabbitmq.recording-queue.name}")
        private String recordingQueueName;

        @Value("${user.rabbitmq.uploading-queue.name}")
        private String uploadingQueueName;

        @Bean
        public Queue testRecordingQueue() {
            return new Queue(recordingQueueName, true);
        }

        @Bean
        public Queue testUploadingQueue() {
            return new Queue(uploadingQueueName, true);
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

    private static final UUID STREAM_ID = UUID.randomUUID();
    private static final String FILENAME = "test_recording.mp4";

    @MockitoBean
    private RecordedFilePartRepository recordedFilePartRepository;
    @MockitoBean
    private StreamSessionRepository streamSessionRepository;
    @MockitoBean
    private JobLogRepository jobLogRepository;

    @Autowired
    private RabbitMqService service;

    @Test
    void should_sendRecordStreamJobToRecordingQueue() {
        RecordStreamJob job = RecordStreamJob.builder()
                .streamId(STREAM_ID)
                .streamerUsername("test")
                .build();

        Assertions.assertTrue(service.sendRecordJob(job));
    }

    @Test
    void should_sendUploadStreamRecordJobToUploadingQueue() {
        UploadStreamRecordJob job = UploadStreamRecordJob.builder()
                .streamId(STREAM_ID)
                .filename(FILENAME)
                .build();

        Assertions.assertTrue(service.sendUploadJob(job));
    }

}