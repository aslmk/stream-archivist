package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordingEventType;
import com.aslmk.common.dto.StreamLifecycleEvent;
import com.aslmk.common.dto.RecordingStatusEvent;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.exception.StreamRecordingException;
import com.aslmk.recordingworker.kafka.KafkaService;
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

    @Value("${user.file.save-directory}")
    private String saveDirectory;

    private static final String DOCKER_IMAGE = "streamlink-runner";
    private static final String RECORDINGS_DIR = "recordings";
    private static final String STREAM_QUALITY = "best";

    private final ProcessExecutor processExecutor;
    private final Clock clock;
    private final KafkaService kafkaService;

    public StreamRecorderService(ProcessExecutor processExecutor, Clock clock, KafkaService kafkaService) {
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
        String saveDirectory = getSaveDirectoryPath() + "/" + RECORDINGS_DIR;

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
                                    String saveDirectory) {
        String command = String.format(
                "streamlink -o %s %s %s",
                "/recordings/" + videoOutputName,
                request.getStreamUrl(),
                STREAM_QUALITY);

        return List.of(
                "docker", "run", "--rm", "-v",
                 saveDirectory + ":/recordings",
                DOCKER_IMAGE,
                "bash", "-c", command);
    }

    private String getSaveDirectoryPath() {
        Path currentDir = Paths.get("").toAbsolutePath();
        Path projectRoot = currentDir.getParent();
        return projectRoot.resolve("/"+saveDirectory).resolve("/"+RECORDINGS_DIR).toString();
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

        if (request.getProviderName() == null || request.getProviderName().isBlank()) {
            log.error("Validation failed: providerName is null or blank in request={}", request);
            throw new InvalidRecordingRequestException("Validation failed: providerName is null or blank");
        }

        if (request.getProviderUserId() == null || request.getProviderUserId().isBlank()) {
            log.error("Validation failed: providerUserId is null or blank in request={}", request);
            throw new InvalidRecordingRequestException("Validation failed: providerUserId is null or blank");
        }
    }

    private void publishRecordingEvent(RecordingEventType eventType,
                                       String videoOutputName,
                                       StreamLifecycleEvent request) {
        RecordingStatusEvent event = RecordingStatusEvent.builder()
                .eventType(eventType)
                .filename(videoOutputName)
                .streamerUsername(request.getStreamerUsername())
                .providerUserId(request.getProviderUserId())
                .providerName(request.getProviderName())
                .build();

        kafkaService.send(event);
    }
}
