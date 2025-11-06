package com.aslmk.gatewayservice.config;

import com.aslmk.gatewayservice.filter.JwtCookieAuthenticationFilter;
import com.aslmk.gatewayservice.filter.JwtUserHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    private final JwtUserHeaderFilter jwtUserHeaderFilter;

    public SecurityConfig(JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter, JwtUserHeaderFilter jwtUserHeaderFilter) {
        this.jwtCookieAuthenticationFilter = jwtCookieAuthenticationFilter;
        this.jwtUserHeaderFilter = jwtUserHeaderFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterBefore(jwtCookieAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(jwtUserHeaderFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }
}
