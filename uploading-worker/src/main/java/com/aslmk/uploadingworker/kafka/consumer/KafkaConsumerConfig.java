package com.aslmk.uploadingworker.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.aslmk.common.dto.RecordCompletedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ConsumerFactory<String, RecordCompletedEvent> consumerFactory() {
        Map<String, Object> propsConfig = new HashMap<>();

        propsConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        propsConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        propsConfig.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        propsConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "com.aslmk.common.dto");

        return new DefaultKafkaConsumerFactory<>(
                propsConfig,
                new StringDeserializer(),
                new JsonDeserializer<>(RecordCompletedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RecordCompletedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, RecordCompletedEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, RecordCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
