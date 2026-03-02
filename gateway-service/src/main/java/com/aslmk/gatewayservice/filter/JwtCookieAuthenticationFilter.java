package com.aslmk.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtCookieAuthenticationFilter implements WebFilter {

    @Value("${user.jwt.cookie-name}")
    private String jwtCookieName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

         if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
             return chain.filter(exchange);
         }

        if (cookies.isEmpty()) {
            log.debug("No cookies present, skipping JWT filter");
            return chain.filter(exchange);
        }

        HttpCookie jwtCookie = cookies.getFirst(jwtCookieName);

        if (jwtCookie != null) {
            log.debug("JWT cookie found. Injecting Authorization header");

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(exchange.getRequest()
                            .mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtCookie.getValue())
                            .build())
                    .build();

            return chain.filter(mutatedExchange);
        }

        return chain.filter(exchange);
    }
}
