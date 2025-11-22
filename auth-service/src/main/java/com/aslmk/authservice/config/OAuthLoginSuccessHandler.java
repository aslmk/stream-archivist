package com.aslmk.authservice.config;

import com.aslmk.authservice.service.CookieService;
import com.aslmk.authservice.service.JwtTokenService;
import com.aslmk.authservice.service.impl.OAuthAuthorizationOrchestrator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuthAuthorizationOrchestrator orchestrator;
    private final JwtTokenService jwtService;
    private final CookieService cookieService;

    public OAuthLoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService,
                                    OAuthAuthorizationOrchestrator orchestrator,
                                    JwtTokenService jwtService,
                                    CookieService cookieService) {
        this.authorizedClientService = authorizedClientService;
        this.orchestrator = orchestrator;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
        String principalName = oauth2Token.getName();

        log.info("Handling OAuth login success: user='{}', provider='{}'", principalName, registrationId);

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);

        if (authorizedClient == null) {
            log.warn("Authorized client not found: user='{}', provider='{}'", principalName, registrationId);
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        orchestrator.authorize(principalName, authorizedClient, oauth2Token.getPrincipal());
        response.addCookie(cookieService.create(jwtService.generate(principalName, registrationId)));

        log.info("Successfully logged in: user='{}', provider='{}'", principalName, registrationId);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
