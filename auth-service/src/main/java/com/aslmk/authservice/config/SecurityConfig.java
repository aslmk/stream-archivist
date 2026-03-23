package com.aslmk.authservice.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final UserLogoutHandler userLogoutHandler;

    public SecurityConfig(OAuthLoginSuccessHandler oAuthLoginSuccessHandler,
                          UserLogoutHandler userLogoutHandler) {
        this.oAuthLoginSuccessHandler = oAuthLoginSuccessHandler;
        this.userLogoutHandler = userLogoutHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth ->
                        oauth.successHandler(oAuthLoginSuccessHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(userLogoutHandler)
                        .deleteCookies("JSESSIONID", "JWT_REFRESH_TOKEN", "JWT_ACCESS_TOKEN")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)
                        )
                )
                .build();
    }
}
