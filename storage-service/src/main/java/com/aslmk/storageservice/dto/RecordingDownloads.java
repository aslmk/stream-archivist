package com.aslmk.storageservice.dto;

import java.util.UUID;

public record RecordingDownloads(UUID streamId, String downloadUrl, String filename) {}
