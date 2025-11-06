package com.aslmk.gatewayservice.filter;

import com.aslmk.gatewayservice.exception.JwtCookieNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    @Value("${user.jwt.cookie-name}")
    private String jwtCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getCookies() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie jwtCookie = Arrays.stream(request.getCookies())
                .filter(c ->
                        c.getName().equals(jwtCookieName))
                .findFirst().orElse(null);

        if (jwtCookie == null) {
            throw new JwtCookieNotFoundException("JWT cookie not found");
        }

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

        filterChain.doFilter(wrapped, response);
    }
}
