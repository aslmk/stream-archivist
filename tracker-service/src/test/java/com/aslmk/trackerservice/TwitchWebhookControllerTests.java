package com.aslmk.trackerservice;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.streamingPlatform.twitch.TwitchWebhookController;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEvent;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
@ExtendWith(MockitoExtension.class)
public class TwitchWebhookControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private KafkaService kafkaService;

    private TwitchEvent twitchEvent;
    private TwitchEventSubRequest twitchEventSubRequest;
    private TwitchSubscription twitchSubscription;

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test0";
    private static final String STREAM_QUALITY = "480p";
    private static final String STREAM_EVENT_TYPE_ONLINE = "stream.online";
    private static final String STREAM_EVENT_TYPE_OFFLINE = "stream.offline";
    private static final String TWITCH_EVENTSUB_ENDPOINT = "/twitch/eventsub";
    private static final String TWITCH_MESSAGE_TYPE = "Twitch-Eventsub-Message-Type";

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
                        .header(TWITCH_MESSAGE_TYPE, "webhook_callback_verification")
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.content().string(twitchEventSubRequest.getChallenge()));
    }

    @Test
    void should_callKafkaServiceAndReturnOkStatus_when_streamIsOnline() throws Exception {
        RecordingRequestDto dto = new RecordingRequestDto();
        dto.setStreamerUsername(STREAMER_USERNAME);
        dto.setStreamUrl(STREAM_URL);
        dto.setStreamQuality(STREAM_QUALITY);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        ArgumentCaptor<RecordingRequestDto> captor = ArgumentCaptor.forClass(RecordingRequestDto.class);

        Mockito.verify(kafkaService).send(captor.capture());

        Assertions.assertNotNull(captor.getValue());

        RecordingRequestDto actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(dto.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(dto.getStreamUrl(), actual.getStreamUrl()),
                () -> Assertions.assertEquals(dto.getStreamQuality(), actual.getStreamQuality())
        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void should_doNothing_when_streamIsOffline() throws Exception {
        twitchEventSubRequest.getSubscription().setType(STREAM_EVENT_TYPE_OFFLINE);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        result.andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(kafkaService, Mockito.never()).send(Mockito.any());
    }

    @Test
    void should_throwUnknownEventTypeExceptionWithUnprocessableEntityStatusCode() throws Exception {
        twitchEventSubRequest.getSubscription().setType("unknown-event-type-blah");

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(twitchEventSubRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn();

        Optional<UnknownEventTypeException> exception = Optional
                .ofNullable((UnknownEventTypeException) result.getResolvedException());

        Assertions.assertTrue(exception.isPresent());
    }

    @Test
    void should_doNothing_when_headerContainsInvalidMessageType() throws Exception {
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TWITCH_MESSAGE_TYPE, "invalid-message-type")
                        .content(mapper.writeValueAsString(twitchEventSubRequest))
        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void should_returnBadRequest_when_jsonIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post(TWITCH_EVENTSUB_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TWITCH_MESSAGE_TYPE, "invalid-message-type")
                        .content(mapper.writeValueAsString("invalid-json-data"))
        );

        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
