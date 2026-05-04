package com.aslmk.recordingworker;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.RecordStreamJob;
import com.aslmk.recordingworker.exception.InvalidRecordStreamJobException;
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
    void recordStream_should_throwException_when_jobIsNull() {
        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(null));
    }

    @Test
    void recordStream_should_throwException_when_streamIdIsNull() {
        RecordStreamJob job = buildRecordStreamJob();
        job.setStreamId(null);

        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(job));
    }

    @Test
    void recordStream_should_throwException_when_streamerUsernameIsNull() {
        RecordStreamJob job = buildRecordStreamJob();
        job.setStreamerUsername(null);

        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(job));
    }

    @Test
    void recordStream_should_throwException_when_streamerUsernameIsBlank() {
        RecordStreamJob job = buildRecordStreamJob();
        job.setStreamerUsername("   ");

        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(job));
    }

    @Test
    void recordStream_should_throwException_when_streamUrlIsNull() {
        RecordStreamJob job = buildRecordStreamJob();
        job.setStreamUrl(null);

        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(job));
    }

    @Test
    void recordStream_should_throwException_when_streamUrlIsBlank() {
        RecordStreamJob job = buildRecordStreamJob();
        job.setStreamUrl("   ");

        Assertions.assertThrows(InvalidRecordStreamJobException.class,
                () -> recorderService.recordStream(job));
    }


    private RecordStreamJob buildRecordStreamJob() {
        return RecordStreamJob.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .streamId(UUID.randomUUID())
                .build();
    }
}