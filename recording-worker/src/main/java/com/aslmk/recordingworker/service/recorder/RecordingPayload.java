package com.aslmk.recordingworker.service.recorder;

import java.nio.file.Path;
import java.util.UUID;

public record RecordingPayload(String url,
                               String quality,
                               Path saveDirectory,
                               String filename,
                               UUID streamerId,
                               String streamerUsername,
                               UUID streamId) {}
