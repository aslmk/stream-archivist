package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.dto.RecordStreamJob;
import com.aslmk.recordingworker.exception.InvalidRecordStreamJobException;
import com.aslmk.recordingworker.service.recorder.RecordingPayload;
import com.aslmk.recordingworker.service.recorder.StreamRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;

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

    public void recordStream(RecordStreamJob job) {
        validateRecordStreamJob(job);

        String videoOutputName = getVideoOutputName(job.getStreamerUsername());
        Path saveDirectory = getStoragePath();

        RecordingPayload payload = new RecordingPayload(job.getStreamUrl(),
                STREAM_QUALITY,
                saveDirectory,
                videoOutputName,
                job.getStreamerUsername(),
                job.getStreamId());

        streamRecorder.record(payload);
    }

    private Path getStoragePath() {
        return Paths.get(properties.getPath()).toAbsolutePath().normalize();
    }

    private String getVideoOutputName(String streamerUsername) {
        return getCurrentInstant() + "_" + streamerUsername + ".mp4";
    }

    private String getCurrentInstant() {
        return String.valueOf(Instant.now(clock).toEpochMilli());
    }

    private void validateRecordStreamJob(RecordStreamJob job) {
        if (job == null) {
            throw new InvalidRecordStreamJobException("request is null");
        }

        if (job.getStreamerUsername() == null || job.getStreamerUsername().isBlank()) {
            throw new InvalidRecordStreamJobException("streamerUsername is null or blank");
        }

        if (job.getStreamUrl() == null || job.getStreamUrl().isBlank()) {
            throw new InvalidRecordStreamJobException("streamUrl is null or blank");
        }

        if (job.getStreamId() == null) {
            throw new InvalidRecordStreamJobException("streamId is null or blank");
        }
    }
}
