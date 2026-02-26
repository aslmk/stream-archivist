package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.constant.GatewayHeaders;
import com.aslmk.subscriptionservice.controller.SubscriptionController;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.SubscriptionRequest;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.service.SubscriptionOrchestrator;
import com.aslmk.subscriptionservice.service.UserSubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubscriptionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean
    private SubscriptionOrchestrator orchestrator;

    @MockitoBean
    private UserSubscriptionService userSubscriptionService;

    private static final String SUBSCRIPTION_ENDPOINT = "/subscriptions";

    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setStreamerUsername("streamer-123");
        subscriptionRequest.setProviderName("twitch");
    }

    @Test
    void should_returnBadRequest_when_requiredUserHeadersMissing() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(subscriptionRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void should_returnNoContent_when_subscriptionCreatedSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(subscriptionRequest)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(orchestrator).subscribe(
                new UserRef("user-123"),
                new StreamerRef("streamer-123", "twitch")
        );
    }
}
