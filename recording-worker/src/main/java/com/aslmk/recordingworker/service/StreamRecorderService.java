package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordingEventType;
import com.aslmk.common.dto.RecordingStatusEvent;
import com.aslmk.common.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.exception.StreamRecordingException;
import com.aslmk.recordingworker.kafka.KafkaService;
import lombok.extern.slf4j.Slf4j;
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

        log.info("Recording started: streamer='{}', url='{}'",
                request.getStreamerUsername(),
                request.getStreamUrl());

        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        Path saveDirectory = getStoragePath();

        publishRecordingEvent(RecordingEventType.RECORDING_STARTED, videoOutputName, request);

        List<String> command = getCommand(request, videoOutputName, saveDirectory);

        int exitCode = processExecutor.execute(command);
        if (exitCode != 0) {
            log.error("Recording process failed: streamer='{}', exitCode={}",
                    request.getStreamerUsername(), exitCode);
            publishRecordingEvent(RecordingEventType.RECORDING_FAILED, videoOutputName, request);
            throw new StreamRecordingException("Recording failed with exit code: " + exitCode);
        }

        log.info("Recording finished successfully: streamer='{}', file='{}'",
                request.getStreamerUsername(),
                videoOutputName);

        publishRecordingEvent(RecordingEventType.RECORDING_FINISHED, videoOutputName, request);
    }

    private List<String> getCommand(StreamLifecycleEvent request,
                                    String videoOutputName,
                                    Path saveDirectory) {

        Path outputPath = saveDirectory.resolve(videoOutputName);

        return List.of("streamlink", "-o",
                outputPath.toString(),
                request.getStreamUrl(),
                STREAM_QUALITY);
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
            log.error("Validation failed: request is null");
            throw new InvalidRecordingRequestException("Validation failed: request is null");
        }

        if (request.getStreamerUsername() == null || request.getStreamerUsername().isBlank()) {
            log.error("Validation failed: streamerUsername is null or blank in request={}", request);
            throw new InvalidRecordingRequestException("Validation failed: streamerUsername is null or blank");
        }

        if (request.getStreamUrl() == null || request.getStreamUrl().isBlank()) {
            log.error("Validation failed: streamUrl is null or blank in request={}", request);
            throw new InvalidRecordingRequestException("Validation failed: streamUrl is null or blank");
        }

        if (request.getStreamerId() == null) {
            log.error("Validation failed: streamerId is null or blank in request={}", request);
            throw new InvalidRecordingRequestException("Validation failed: streamerId is null or blank");
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
