package com.aslmk.recordingworker;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.StreamRecorderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StreamRecorderServiceUnitTests {

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025,
            8,
            20,
            12,
            0,
            0,
            0,
            ZoneId.of("UTC")
    );

    private static final String VIDEO_OUTPUT_NAME = "20_08_2025_test0.ts";

    @Mock
    private RecordingStorageProperties properties;
    @Mock
    private ProcessExecutor processExecutor;
    @Mock
    private Clock clock;
    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private StreamRecorderService recorderService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(clock.instant()).thenReturn(NOW.toInstant());
        Mockito.lenient().when(clock.getZone()).thenReturn(NOW.getZone());
        Mockito.lenient().when(properties.getPath()).thenReturn("common/recordings");
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_requestIsNull() {
        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(null));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamerIdIsNull() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        request.setStreamerId(null);

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamerUsernameIsNull() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        request.setStreamerUsername(null);

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamerUsernameIsBlank() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        request.setStreamerUsername("   ");

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamUrlIsNull() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        request.setStreamUrl(null);

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamUrlIsBlank() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        request.setStreamUrl("   ");

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }


    private StreamLifecycleEvent buildStreamLifecycleEvent() {
        return StreamLifecycleEvent.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .streamerId(UUID.randomUUID())
                .build();
    }
}