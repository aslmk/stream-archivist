package com.aslmk.gatewayservice.filter;

import com.aslmk.gatewayservice.constant.GatewayHeaders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtUserHeaderFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    String userId = auth.getToken().getSubject();

                    return exchange.mutate()
                            .request(exchange.getRequest()
                                    .mutate()
                                    .header(GatewayHeaders.USER_ID, userId)
                                    .build())
                            .build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
