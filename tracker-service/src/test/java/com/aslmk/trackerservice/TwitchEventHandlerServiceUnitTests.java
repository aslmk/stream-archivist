package com.aslmk.trackerservice;

import com.aslmk.trackerservice.client.twitch.dto.TwitchEvent;
import com.aslmk.trackerservice.client.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.client.twitch.dto.TwitchSubscription;
import com.aslmk.trackerservice.domain.StreamerEntity;
import com.aslmk.trackerservice.dto.StreamLifecycleEvent;
import com.aslmk.trackerservice.dto.StreamLifecycleType;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.event.EventLogService;
import com.aslmk.trackerservice.service.event.EventProcessedService;
import com.aslmk.trackerservice.service.event.TwitchEventHandlerServiceImpl;
import com.aslmk.trackerservice.service.streamer.StreamerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class TwitchEventHandlerServiceUnitTests {

    @InjectMocks
    private TwitchEventHandlerServiceImpl handler;
    @Mock
    private KafkaService kafkaService;
    @Mock
    private StreamerService streamerService;
    @Mock
    private EventProcessedService eventService;
    @Mock
    private EventLogService eventLogService;

    private TwitchEventSubRequest twitchEventSubRequest;

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test0";
    private static final String STREAM_EVENT_TYPE_ONLINE = "stream.online";
    private static final String TWITCH_EVENT_ID = "twitch_event_id_123";
    private static final UUID STREAMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TwitchEvent twitchEvent = new TwitchEvent();
        twitchEvent.setBroadcaster_user_id("12345");
        twitchEvent.setBroadcaster_user_login(STREAMER_USERNAME);

        TwitchSubscription twitchSubscription = new TwitchSubscription();
        twitchSubscription.setType(STREAM_EVENT_TYPE_ONLINE);

        twitchEventSubRequest = new TwitchEventSubRequest();
        twitchEventSubRequest.setChallenge("40fh0hfad8fh");
        twitchEventSubRequest.setEvent(twitchEvent);
        twitchEventSubRequest.setSubscription(twitchSubscription);
    }

    @Test
    void should_saveEventPayload_when_TwitchEventIsReceived() {
        Mockito.when(streamerService.findByProviderUserIdAndProviderName("12345", "twitch"))
                .thenReturn(Optional.ofNullable(StreamerEntity.builder()
                                .id(STREAMER_ID)
                        .build()));
        Mockito.when(eventService.tryMarkAsProcessed(TWITCH_EVENT_ID)).thenReturn(true);

        StreamLifecycleEvent expected = StreamLifecycleEvent.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamerId(STREAMER_ID)
                .eventType(StreamLifecycleType.STREAM_STARTED)
                .streamUrl(STREAM_URL)
                .build();

        handler.handle(twitchEventSubRequest, TWITCH_EVENT_ID);

        ArgumentCaptor<StreamLifecycleEvent> captor = ArgumentCaptor.forClass(StreamLifecycleEvent.class);
        Mockito.verify(eventLogService).save(captor.capture(), Mockito.any());

        StreamLifecycleEvent actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(expected.getEventType(), actual.getEventType()),
                () -> Assertions.assertEquals(expected.getStreamerId(), actual.getStreamerId()),
                () -> Assertions.assertEquals(expected.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(expected.getStreamUrl(), actual.getStreamUrl()));
    }

    @Test
    void should_throwUnknownEventTypeException_when_eventTypeIsUnknown() {
        Mockito.when(eventService.tryMarkAsProcessed(TWITCH_EVENT_ID)).thenReturn(true);

        twitchEventSubRequest.getSubscription().setType("unknown-event-type-blah");

        Assertions.assertThrows(UnknownEventTypeException.class,
                () -> handler.handle(twitchEventSubRequest, TWITCH_EVENT_ID));
    }

    @Test
    void should_returnImmediately_when_eventIsAlreadyProcessed() {
        Mockito.when(eventService.tryMarkAsProcessed(TWITCH_EVENT_ID)).thenReturn(false);

        handler.handle(Mockito.any(), TWITCH_EVENT_ID);

        Mockito.verify(kafkaService, Mockito.never()).send(Mockito.any());
    }

}
