package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.RecordingEventType;
import com.aslmk.recordingworker.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.messaging.kafka.KafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StreamRecorderService {

    @Value("${user.recording.mode}")
    private String RECORDING_MODE;

    private final RecordingStorageProperties properties;
    private static final String STREAM_QUALITY = "best";

    private final ProcessExecutor processExecutor;
    private final Clock clock;
    private final KafkaService kafkaService;

    public StreamRecorderService(RecordingStorageProperties properties, ProcessExecutor processExecutor, Clock clock, KafkaService kafkaService) {
        this.properties = properties;
        this.processExecutor = processExecutor;
        this.clock = clock;
        this.kafkaService = kafkaService;
    }

    public void recordStream(StreamLifecycleEvent request) {
        validateRecordingRequest(request);

        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        Path saveDirectory = getStoragePath();

        List<String> command;
        if (RECORDING_MODE.equals("SINGLE")) {
            command = runSingleRecording(request, videoOutputName, saveDirectory);
        } else if (RECORDING_MODE.equals("CHUNKED")) {
            command = runChunkedRecording(request, saveDirectory);
        } else {
            throw new IllegalArgumentException("Unsupported recording mode: " + RECORDING_MODE);
        }

        log.info("Recording started for streamer: id='{}', username='{}'",
                request.getStreamerId(),
                request.getStreamerUsername());

        publishRecordingEvent(RecordingEventType.RECORDING_STARTED, videoOutputName, request);

        boolean result = processExecutor.execute(command);

        if (result) {
            log.info("Recording finished: streamerId='{}', file='{}'",
                    request.getStreamerId(), videoOutputName);
            publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, videoOutputName, request);
        } else {
            log.error("Recording failed: streamerId='{}'", request.getStreamerId());
            publishRecordingEvent(RecordingEventType.RECORDING_FAILED, videoOutputName, request);
        }
    }

    private List<String> runSingleRecording(StreamLifecycleEvent request,
                                             String videoOutputName,
                                             Path saveDirectory) {

        Path outputPath = saveDirectory.resolve(videoOutputName);

        return List.of("streamlink", "-o",
                outputPath.toString(),
                request.getStreamUrl(),
                STREAM_QUALITY);
    }

    private List<String> runChunkedRecording(StreamLifecycleEvent request,
                                             Path saveDirectory) {
        String filename = request.getStreamerUsername() + "_%08d.ts";
        String output = saveDirectory.resolve(filename).toString();

        String command = "streamlink " + request.getStreamUrl() + " " + STREAM_QUALITY +
                " --stdout | ffmpeg -i pipe:0 -c copy -f segment -segment_time 25 " +
                "-reset_timestamps 1 " + output;

        return List.of("bash", "-c", command);
    }

    private Path getStoragePath() {
        return Paths.get(properties.getPath()).toAbsolutePath().normalize();
    }

    private String getVideoOutputName(String streamerUsername) {
        return getCurrentDateTime() + "_" + streamerUsername + ".ts";
    }

    private String getCurrentDateTime() {
        LocalDateTime dateTime = LocalDateTime.now(clock);
        return DateTimeFormatter.ofPattern("dd_MM_yyyy").format(dateTime);
    }

    private void validateRecordingRequest(StreamLifecycleEvent request) {
        if (request == null) {
            throw new InvalidRecordingRequestException("request is null");
        }

        if (request.getStreamerUsername() == null || request.getStreamerUsername().isBlank()) {
            throw new InvalidRecordingRequestException("streamerUsername is null or blank");
        }

        if (request.getStreamUrl() == null || request.getStreamUrl().isBlank()) {
            throw new InvalidRecordingRequestException("streamUrl is null or blank");
        }

        if (request.getStreamerId() == null) {
            throw new InvalidRecordingRequestException("streamerId is null or blank");
        }
    }

    private void publishRecordingEvent(RecordingEventType eventType,
                                       String videoOutputName,
                                       StreamLifecycleEvent request) {
        RecordingStatusEvent event = RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(videoOutputName)
                .streamerUsername(request.getStreamerUsername())
                .streamerId(request.getStreamerId())
                .build();

        kafkaService.send(event);
    }
}
