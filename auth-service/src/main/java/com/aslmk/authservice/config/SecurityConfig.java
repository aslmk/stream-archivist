package com.aslmk.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;

    public SecurityConfig(OAuthLoginSuccessHandler oAuthLoginSuccessHandler) {
        this.oAuthLoginSuccessHandler = oAuthLoginSuccessHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .oauth2Login(oauth ->
                        oauth.successHandler(oAuthLoginSuccessHandler))
                .build();
    }
}
