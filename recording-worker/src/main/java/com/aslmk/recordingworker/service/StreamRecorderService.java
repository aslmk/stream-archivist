package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordingRequestDto;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.exception.StreamRecordingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StreamRecorderService {

    private final static String DOCKER_IMAGE = "streamlink-ffmpeg-runner";
    private final static String RECORDINGS_DIR = "recordings";

    private final ProcessExecutor processExecutor;
    private final Clock clock;

    public StreamRecorderService(ProcessExecutor processExecutor, Clock clock) {
        this.processExecutor = processExecutor;
        this.clock = clock;
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

        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        String saveDirectory = getCurrentDirectoryPath() + "/" + RECORDINGS_DIR;

        List<String> command = getCommand(request, videoOutputName, saveDirectory);
        int exitCode = processExecutor.execute(command);

        handleExitCode(exitCode, request.getStreamerUsername());
    }

    private void handleExitCode(int exitCode, String streamerUsername) {
        if (exitCode != 0) {
            log.warn("Process exited with code {}", exitCode);
            throw new StreamRecordingException("Recording failed with exit code: " + exitCode);
        } else {
            log.info("'{}' stream was recorded successfully", streamerUsername);
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

    private String getCurrentDirectoryPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    private String getVideoOutputName(String streamerUsername) {
        return getCurrentDateTime() + "_" + streamerUsername + ".mp4";
    }

    private String getCurrentDateTime() {
        LocalDateTime dateTime = LocalDateTime.now(clock);
        return DateTimeFormatter.ofPattern("dd_MM_yyyy").format(dateTime);
    }
}
