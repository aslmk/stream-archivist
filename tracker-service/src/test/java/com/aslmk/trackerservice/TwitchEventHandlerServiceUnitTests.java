package com.aslmk.trackerservice;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.trackerservice.entity.StreamerEntity;
import com.aslmk.trackerservice.exception.UnknownEventTypeException;
import com.aslmk.trackerservice.kafka.KafkaService;
import com.aslmk.trackerservice.service.StreamerService;
import com.aslmk.trackerservice.service.impl.TwitchEventHandlerServiceImpl;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEvent;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchEventSubRequest;
import com.aslmk.trackerservice.streamingPlatform.twitch.dto.TwitchSubscription;
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

@ExtendWith(MockitoExtension.class)
public class TwitchEventHandlerServiceUnitTests {

    @InjectMocks
    private TwitchEventHandlerServiceImpl handler;
    @Mock
    private KafkaService kafkaService;
    @Mock
    private StreamerService streamerService;

    private TwitchEvent twitchEvent;
    private TwitchEventSubRequest twitchEventSubRequest;
    private TwitchSubscription twitchSubscription;

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test0";
    private static final String STREAM_EVENT_TYPE_ONLINE = "stream.online";
    private static final String STREAM_EVENT_TYPE_OFFLINE = "stream.offline";


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
    void should_callKafkaService_when_streamIsOnline() {
        Mockito.when(streamerService.findByProviderUserIdAndProviderName("12345", "twitch"))
                .thenReturn(Optional.ofNullable(StreamerEntity.builder().build()));

        RecordingRequestDto dto = new RecordingRequestDto();
        dto.setStreamerUsername(STREAMER_USERNAME);
        dto.setStreamUrl(STREAM_URL);

        ArgumentCaptor<RecordingRequestDto> captor = ArgumentCaptor.forClass(RecordingRequestDto.class);

        handler.handle(twitchEventSubRequest);

        Mockito.verify(kafkaService).send(captor.capture());

        Assertions.assertNotNull(captor.getValue());

        RecordingRequestDto actual = captor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(dto.getStreamerUsername(), actual.getStreamerUsername()),
                () -> Assertions.assertEquals(dto.getStreamUrl(), actual.getStreamUrl())
        );
    }

    @Test
    void should_doNothing_when_streamIsOffline(){
        Mockito.when(streamerService.findByProviderUserIdAndProviderName("12345", "twitch"))
                .thenReturn(Optional.ofNullable(StreamerEntity.builder().build()));

        twitchEventSubRequest.getSubscription().setType(STREAM_EVENT_TYPE_OFFLINE);
        handler.handle(twitchEventSubRequest);
        Mockito.verify(kafkaService, Mockito.never()).send(Mockito.any());
    }

    @Test
    void should_throwUnknownEventTypeExceptionWithUnprocessableEntityStatusCode() {
        twitchEventSubRequest.getSubscription().setType("unknown-event-type-blah");

        Assertions.assertThrows(UnknownEventTypeException.class,
                () -> handler.handle(twitchEventSubRequest));
    }

}
