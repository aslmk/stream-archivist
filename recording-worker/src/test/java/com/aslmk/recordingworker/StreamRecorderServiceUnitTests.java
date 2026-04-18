package com.aslmk.recordingworker;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.StreamRecorderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StreamRecorderServiceUnitTests {

    private static final String STREAMER_USERNAME = "test0";
    private static final String STREAM_URL = "https://twitch.tv/test";
    private static final String STREAM_QUALITY = "best";

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
        ReflectionTestUtils.setField(recorderService, "RECORDING_MODE", "SINGLE");
    }

    @Test
    void recordStream_should_buildValidCommandForProcessExecutor() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(true);

        recorderService.recordStream(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(processExecutor).execute(captor.capture());

        List<String> actualCmd = captor.getValue();
        String cmdStr = String.join(" ", actualCmd);

        Assertions.assertAll(
                () -> Assertions.assertTrue(cmdStr.contains("streamlink")),
                () -> Assertions.assertTrue(cmdStr.contains("-o")),
                () -> Assertions.assertTrue(cmdStr.contains(VIDEO_OUTPUT_NAME)),
                () -> Assertions.assertTrue(cmdStr.contains(STREAM_URL)),
                () -> Assertions.assertTrue(cmdStr.contains(STREAM_QUALITY))
        );
    }

    @Test
    void recordStream_should_publishRecordingStartedEvent() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(true);

        recorderService.recordStream(request);

        ArgumentCaptor<RecordingStatusEvent> captor = ArgumentCaptor.forClass(RecordingStatusEvent.class);
        Mockito.verify(kafkaService, Mockito.atLeastOnce()).send(captor.capture());

        boolean hasStartedEvent = captor.getAllValues().stream()
                .anyMatch(e -> e.getEventType() == RecordingEventType.RECORDING_STARTED);

        Assertions.assertTrue(hasStartedEvent);
    }

    @Test
    void recordStream_should_publishRecordingFinishedEvent_when_processSucceeds() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(true);

        recorderService.recordStream(request);

        ArgumentCaptor<RecordingStatusEvent> captor = ArgumentCaptor.forClass(RecordingStatusEvent.class);
        Mockito.verify(kafkaService, Mockito.atLeastOnce()).send(captor.capture());

        boolean hasFinishedEvent = captor.getAllValues().stream()
                .anyMatch(e -> e.getEventType() == RecordingEventType.RECORDING_FINISHED);

        Assertions.assertTrue(hasFinishedEvent);
    }

    @Test
    void recordStream_should_publishRecordingFailedEvent_when_processFails() {
        StreamLifecycleEvent request = buildStreamLifecycleEvent();
        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(false);

        recorderService.recordStream(request);

        ArgumentCaptor<RecordingStatusEvent> captor = ArgumentCaptor.forClass(RecordingStatusEvent.class);
        Mockito.verify(kafkaService, Mockito.atLeastOnce()).send(captor.capture());

        boolean hasFailedEvent = captor.getAllValues().stream()
                .anyMatch(e -> e.getEventType() == RecordingEventType.RECORDING_FAILED);

        Assertions.assertTrue(hasFailedEvent);
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

    @Test
    void recordStream_should_throwIllegalArgumentException_when_unsupportedRecordingMode() {
        ReflectionTestUtils.setField(recorderService, "RECORDING_MODE", "UNSUPPORTED");

        StreamLifecycleEvent request = buildStreamLifecycleEvent();

        Assertions.assertThrows(IllegalArgumentException.class,
                ()-> recorderService.recordStream(request));
    }

    private StreamLifecycleEvent buildStreamLifecycleEvent() {
        return StreamLifecycleEvent.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .streamerId(UUID.randomUUID())
                .build();
    }
}