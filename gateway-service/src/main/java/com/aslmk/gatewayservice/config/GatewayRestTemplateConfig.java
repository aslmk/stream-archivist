package com.aslmk.gatewayservice.config;

import com.aslmk.common.constants.GatewayHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRestTemplateConfig {

    private final HttpServletRequest request;

    public GatewayRestTemplateConfig(HttpServletRequest request) {
        this.request = request;
    }

    @Bean
    public RestTemplateCustomizer customRestTemplateHeaders() {
        return restTemplate -> restTemplate.getInterceptors().add((req, body, execution) -> {
            String userId = request.getHeader(GatewayHeaders.USER_ID);

            if (userId != null) {
                req.getHeaders().add(GatewayHeaders.USER_ID, userId);
            }

            return execution.execute(req, body);
        });
    }
}
