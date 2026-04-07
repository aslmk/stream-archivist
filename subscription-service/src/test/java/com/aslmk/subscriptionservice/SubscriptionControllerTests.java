package com.aslmk.subscriptionservice;

import com.aslmk.subscriptionservice.constant.GatewayHeaders;
import com.aslmk.subscriptionservice.controller.SubscriptionController;
import com.aslmk.subscriptionservice.dto.StreamerRef;
import com.aslmk.subscriptionservice.dto.SubscriptionRequest;
import com.aslmk.subscriptionservice.dto.UserRef;
import com.aslmk.subscriptionservice.dto.UserSubscriptionsResponse;
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

import java.util.List;

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

    private static final String SUBSCRIPTION_ENDPOINT = "/api/subscriptions";
    private static final String USER_ID = "user-123";
    private static final String STREAMER_ID = "streamer-uuid-456";

    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setStreamerUsername("streamer-123");
        subscriptionRequest.setProviderName("twitch");
    }

    @Test
    void subscribe_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(subscriptionRequest)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(orchestrator).subscribe(
                new UserRef(USER_ID),
                new StreamerRef("streamer-123", "twitch")
        );
    }

    @Test
    void subscribe_shouldReturnInternalError_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(subscriptionRequest)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        Mockito.verifyNoInteractions(orchestrator);
    }

    @Test
    void subscribe_shouldReturnBadRequest_whenBodyIsInvalid() throws Exception {
        subscriptionRequest.setStreamerUsername("");
        subscriptionRequest.setProviderName("");

        mockMvc.perform(MockMvcRequestBuilders.post(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(subscriptionRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(orchestrator);
    }

    @Test
    void getUserSubscriptions_shouldReturnOk_whenRequestIsValid() throws Exception {
        UserSubscriptionsResponse response = new UserSubscriptionsResponse(List.of());
        Mockito.when(userSubscriptionService.getAllUserSubscriptions(USER_ID)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userSubscriptionService).getAllUserSubscriptions(USER_ID);
    }

    @Test
    void getUserSubscriptions_shouldReturnInternalError_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SUBSCRIPTION_ENDPOINT))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        Mockito.verifyNoInteractions(userSubscriptionService);
    }

    @Test
    void unsubscribe_shouldReturnNoContent_whenRequestIsValid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, USER_ID)
                        .param("streamerId", STREAMER_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(orchestrator).unsubscribe(USER_ID, STREAMER_ID);
    }

    @Test
    void unsubscribe_shouldReturnInternalError_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(SUBSCRIPTION_ENDPOINT)
                        .param("streamerId", STREAMER_ID))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        Mockito.verifyNoInteractions(orchestrator);
    }

    @Test
    void unsubscribe_shouldReturnBadRequest_whenStreamerIdParamMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(SUBSCRIPTION_ENDPOINT)
                        .header(GatewayHeaders.USER_ID, USER_ID))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verifyNoInteractions(orchestrator);
    }
}