package com.aslmk.recordingworker;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.exception.StreamRecordingException;
import com.aslmk.recordingworker.kafka.KafkaService;
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

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class StreamRecorderServiceUnitTests {

    private static final String DOCKER_IMAGE = "streamlink-runner";

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
    }

    @Test
    void recordStream_should_succeed_when_exitCodeIsZero() {
        RecordingRequestDto request = buildRecordingRequestDto();

        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(0);

        recorderService.recordStream(request);

        Mockito.verify(processExecutor, Mockito.times(1))
                .execute(Mockito.anyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);

        Mockito.verify(processExecutor).execute(captor.capture());

        List<String> actualCmd = captor.getValue();

        Assertions.assertTrue(actualCmd.contains("docker") && actualCmd.contains(DOCKER_IMAGE));
    }

    @Test
    void recordStream_should_throwStreamRecordingException_when_exitCodeIsNonZero() {
        RecordingRequestDto request = buildRecordingRequestDto();

        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(1);

        Assertions.assertThrows(StreamRecordingException.class, () -> recorderService.recordStream(request));

        Mockito.verify(processExecutor, Mockito.times(1)).execute(Mockito.anyList());
    }

    @Test
    void recordStream_should_generateValidFileName_when_getVideoOutputNameIsCalled() {
        RecordingRequestDto request = buildRecordingRequestDto();

        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(0);

        recorderService.recordStream(request);

        Mockito.verify(processExecutor, Mockito.times(1))
                .execute(Mockito.anyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);

        Mockito.verify(processExecutor).execute(captor.capture());

        List<String> actualCmd = captor.getValue();

        String cmdStr = String.join(" ", actualCmd);

        Assertions.assertTrue(cmdStr.contains(VIDEO_OUTPUT_NAME));
    }

    @Test
    void recordStream_should_buildValidCommandForProcessExecutor_when_getCommandIsCalled() {
        RecordingRequestDto request = buildRecordingRequestDto();

        Mockito.when(processExecutor.execute(Mockito.anyList())).thenReturn(0);

        recorderService.recordStream(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(processExecutor).execute(captor.capture());

        List<String> actualCmd = captor.getValue();
        String cmdStr = String.join(" ", actualCmd);

        Assertions.assertAll(
                () -> Assertions.assertTrue(cmdStr.contains("docker run --rm -v")),
                () -> Assertions.assertTrue(cmdStr.contains("/recordings:/recordings")),
                () -> Assertions.assertTrue(cmdStr.contains(DOCKER_IMAGE)),
                () -> Assertions.assertTrue(cmdStr.contains("bash -c")),
                () -> Assertions.assertTrue(
                        cmdStr.contains(String.format(
                                "streamlink -o %s %s %s",
                                "/recordings/" + VIDEO_OUTPUT_NAME,
                                STREAM_URL,
                                STREAM_QUALITY))
                ),
                () -> Assertions.assertTrue(cmdStr.contains(VIDEO_OUTPUT_NAME))
        );
    }

    private RecordingRequestDto buildRecordingRequestDto() {
        return RecordingRequestDto.builder()
                .streamerUsername(STREAMER_USERNAME)
                .streamUrl(STREAM_URL)
                .providerUserId("123")
                .providerName("twitch")
                .build();
    }


    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamerUsernameIsNull() {
        RecordingRequestDto request = buildRecordingRequestDto();
        request.setStreamerUsername(null);

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamUrlIsNull() {
        RecordingRequestDto request = buildRecordingRequestDto();
        request.setStreamUrl(null);

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamerUsernameIsEmpty() {
        RecordingRequestDto request = buildRecordingRequestDto();
        request.setStreamerUsername("");

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_streamUrlIsEmpty() {
        RecordingRequestDto request = buildRecordingRequestDto();
        request.setStreamUrl("");

        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(request));
    }

    @Test
    void recordStream_should_throwInvalidRecordingRequestException_when_recordingRequestDtoIsNull() {
        Assertions.assertThrows(InvalidRecordingRequestException.class,
                () -> recorderService.recordStream(null));
    }
}
