package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordCompletedEvent;
import com.aslmk.common.dto.RecordingRequestDto;
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

    private static final String DOCKER_IMAGE = "streamlink-ffmpeg-runner";
    private static final String RECORDINGS_DIR = "recordings";

    private final ProcessExecutor processExecutor;
    private final Clock clock;
    private final KafkaService kafkaService;

    public StreamRecorderService(ProcessExecutor processExecutor, Clock clock, KafkaService kafkaService) {
        this.processExecutor = processExecutor;
        this.clock = clock;
        this.kafkaService = kafkaService;
    }

    public void recordStream(RecordingRequestDto request) {

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

        if (request.getStreamQuality() == null || request.getStreamQuality().isBlank()) {
            log.warn("Defaulting stream quality to 'best', because it was null or blank in request={}", request);
            request.setStreamQuality("best");
        }

        log.info("Recording started: streamer='{}', url='{}', quality='{}'",
                request.getStreamerUsername(),
                request.getStreamUrl(),
                request.getStreamQuality());

        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        String saveDirectory = getSaveDirectoryPath() + "/" + RECORDINGS_DIR;

        List<String> command = getCommand(request, videoOutputName, saveDirectory);
        int exitCode = processExecutor.execute(command);

        handleExitCode(exitCode, request.getStreamerUsername());

        log.info("Recording finished successfully: streamer='{}', file='{}'",
                request.getStreamerUsername(),
                videoOutputName);

        RecordCompletedEvent recordCompletedEvent = RecordCompletedEvent.builder()
                .streamerUsername(request.getStreamerUsername())
                .fileName(videoOutputName)
                .build();

        log.info("Publishing completion event: streamer='{}', file='{}'",
                request.getStreamerUsername(),
                videoOutputName);

        kafkaService.send(recordCompletedEvent);
    }

    private void handleExitCode(int exitCode, String streamerUsername) {
        if (exitCode != 0) {
            log.error("Recording process failed: streamer='{}', exitCode={}", streamerUsername, exitCode);
            throw new StreamRecordingException("Recording failed with exit code: " + exitCode);
        } else {
            log.info("Recording completed: streamer='{}'", streamerUsername);
        }
    }

    private List<String> getCommand(RecordingRequestDto request,
                                           String videoOutputName,
                                           String saveDirectory) {
        String command = String.format(
                "streamlink -O %s %s | ffmpeg -i - -c copy -ss 15 /recordings/%s",
                request.getStreamUrl(),
                request.getStreamQuality(),
                videoOutputName);

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
        return getCurrentDateTime() + "_" + streamerUsername + ".mp4";
    }

    private String getCurrentDateTime() {
        LocalDateTime dateTime = LocalDateTime.now(clock);
        return DateTimeFormatter.ofPattern("dd_MM_yyyy").format(dateTime);
    }
}
