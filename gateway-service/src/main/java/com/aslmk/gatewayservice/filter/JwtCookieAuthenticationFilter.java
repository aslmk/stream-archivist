package com.aslmk.gatewayservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/actuator/",
            "/oauth2/"
    );

    @Value("${user.jwt.cookie-name}")
    private String jwtCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("Processing request {} {} from IP {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        if (isPublicEndpoint(request)) {
            log.debug("Public endpoint detected, skipping JWT filter: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() == null) {
            log.debug("No cookies present, skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }

        Cookie jwtCookie = Arrays.stream(request.getCookies())
                .filter(c ->
                        c.getName().equals(jwtCookieName))
                .findFirst().orElse(null);

        if (jwtCookie == null) {
            log.warn("JWT cookie not found for request {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.debug("JWT cookie found. Injecting Authorization header");

        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return "Bearer " + jwtCookie.getValue();
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of("Bearer " + jwtCookie.getValue()));
                }
                return super.getHeaders(name);
            }
        };

        log.debug("Forwarding wrapped request");
        filterChain.doFilter(wrapped, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return PUBLIC_PATHS.stream().anyMatch(uri -> request.getRequestURI().startsWith(uri));
    }
}
