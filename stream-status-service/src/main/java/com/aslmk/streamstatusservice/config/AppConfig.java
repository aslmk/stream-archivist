package com.aslmk.streamstatusservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient subscriptionWebClient(
            WebClient.Builder builder,
            @Value("${user.subscription-service.url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
