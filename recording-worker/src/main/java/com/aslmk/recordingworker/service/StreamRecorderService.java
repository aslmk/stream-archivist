package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.StreamLifecycleEvent;
import com.aslmk.recordingworker.exception.InvalidRecordingRequestException;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import com.aslmk.recordingworker.service.recorder.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class StreamRecorderService {

    private final RecordingStorageProperties properties;
    private static final String STREAM_QUALITY = "best";

    private final Clock clock;
    private final StreamRecorder streamRecorder;


    public StreamRecorderService(RecordingStorageProperties properties,
                                 Clock clock,
                                 StreamRecorder streamRecorder) {
        this.properties = properties;
        this.clock = clock;
        this.streamRecorder = streamRecorder;
    }

    public void recordStream(StreamLifecycleEvent request) {
        validateRecordingRequest(request);

        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        Path saveDirectory = getStoragePath();

        RecordingPayload payload = new RecordingPayload(request.getStreamUrl(),
                STREAM_QUALITY,
                saveDirectory,
                videoOutputName,
                request.getStreamerId(),
                request.getStreamerUsername(),
                request.getStreamId());

        streamRecorder.record(payload);
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
}
