package com.aslmk.gatewayservice.config;

import com.aslmk.gatewayservice.filter.JwtCookieAuthenticationFilter;
import com.aslmk.gatewayservice.filter.JwtUserHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    private final JwtUserHeaderFilter jwtUserHeaderFilter;

    public SecurityConfig(JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter,
                          JwtUserHeaderFilter jwtUserHeaderFilter) {
        this.jwtCookieAuthenticationFilter = jwtCookieAuthenticationFilter;
        this.jwtUserHeaderFilter = jwtUserHeaderFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange((exchange) -> exchange
                        .pathMatchers(
                                "/actuator/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/auth/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterBefore(jwtCookieAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
                .addFilterAfter(jwtUserHeaderFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
