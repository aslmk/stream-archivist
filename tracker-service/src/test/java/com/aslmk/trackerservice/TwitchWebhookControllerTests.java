package com.aslmk.trackerservice;

import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.service.event.TwitchEventHandlerService;
import com.aslmk.trackerservice.controller.TwitchWebhookController;
import com.aslmk.trackerservice.client.twitch.dto.TwitchEvent;
import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.client.twitch.dto.TwitchSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

@WebMvcTest(controllers = TwitchWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TwitchWebhookControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private TwitchEventHandlerService handler;

    private TwitchEvent twitchEvent;
    private TwitchEventSubRequest twitchEventSubRequest;
    private TwitchSubscription twitchSubscription;

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_EVENT_TYPE_ONLINE = "stream.online";
    private static final String STREAM_EVENT_TYPE_OFFLINE = "stream.offline";
    private static final String TWITCH_EVENTSUB_ENDPOINT = "/twitch/eventsub";
    private static final String TWITCH_MESSAGE_TYPE_HEADER = "Twitch-Eventsub-Message-Type";
    private static final String TWITCH_MESSAGE_ID_HEADER = "Twitch-Eventsub-Message-Id";
    private static final String TWITCH_EVENT_ID = "twitch_event_id_123";

    @BeforeEach
    void setUp() {
        twitchEvent = new TwitchEvent();
        twitchEvent.setBroadcaster_user_id("12345");
        twitchEvent.setBroadcaster_user_login(STREAMER_USERNAME);

        twitchSubscription = new TwitchSubscription();
        twitchSubscription.setType(STREAM_EVENT_TYPE_ONLINE);

        twitchEventSubRequest = new TwitchEventSubRequest();
        twitchEventSubRequest.setChallenge("40fh0hfad8fh");
        twitchEventSubRequest.setEvent(twitchEvent);
        twitchEventSubRequest.setSubscription(twitchSubscription);
    }

    @Test
    void should_returnChallenge_when_requestHeaderContainsWebhookCallbackVerification() throws Exception {
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TWITCH_MESSAGE_TYPE_HEADER, "webhook_callback_verification")
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.content().string(twitchEventSubRequest.getChallenge()));
    }

    @Test
    void should_returnNoContent_when_headerContainsInvalidMessageType() throws Exception {
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TWITCH_MESSAGE_TYPE_HEADER, "invalid-message-type")
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        result.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void should_callTwitchEventHandler_when_streamIsOnline() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .header(TWITCH_MESSAGE_ID_HEADER, TWITCH_EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        Mockito.verify(handler).handle(Mockito.any(), Mockito.eq(TWITCH_EVENT_ID));
    }

    @Test
    void should_callTwitchEventHandler_when_streamIsOffline() throws Exception {
        twitchEventSubRequest.getSubscription().setType(STREAM_EVENT_TYPE_OFFLINE);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .header(TWITCH_MESSAGE_ID_HEADER, TWITCH_EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        Mockito.verify(handler).handle(Mockito.any(), Mockito.eq(TWITCH_EVENT_ID));

        result.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void should_returnUnprocessableEntity_when_handlerThrowsUnknownEventTypeException() throws Exception {
        Mockito.doThrow(UnknownEventTypeException.class)
                .when(handler).handle(Mockito.any(), Mockito.eq(TWITCH_EVENT_ID));

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                                .header(TWITCH_MESSAGE_ID_HEADER, TWITCH_EVENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(twitchEventSubRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn();

        Optional<UnknownEventTypeException> exception = Optional
                .ofNullable((UnknownEventTypeException) result.getResolvedException());

        Assertions.assertTrue(exception.isPresent());
    }
}
