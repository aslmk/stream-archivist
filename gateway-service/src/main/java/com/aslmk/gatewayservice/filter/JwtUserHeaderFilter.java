package com.aslmk.gatewayservice.filter;

import com.aslmk.common.constants.GatewayHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
public class JwtUserHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            log.debug("No authentication found in SecurityContext, skipping user header injection");
            filterChain.doFilter(request, response);
            return;
        }

        Jwt jwt = (Jwt) auth.getPrincipal();

        log.info("Injecting user headers: user_id={}, provider={}",
                jwt.getSubject(),
                jwt.getClaimAsString("provider_name")
        );

        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (GatewayHeaders.USER_ID.equalsIgnoreCase(name)) {
                    return jwt.getSubject();
                }
                if (GatewayHeaders.PROVIDER_NAME.equalsIgnoreCase(name)) {
                    return jwt.getClaimAsString("provider_name");
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (GatewayHeaders.USER_ID.equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(jwt.getSubject()));
                }
                if (GatewayHeaders.PROVIDER_NAME.equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(jwt.getClaimAsString("provider_name")));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.add(GatewayHeaders.USER_ID);
                names.add(GatewayHeaders.PROVIDER_NAME);
                return Collections.enumeration(names);
            }
        };

        filterChain.doFilter(wrapped, response);
    }
}
