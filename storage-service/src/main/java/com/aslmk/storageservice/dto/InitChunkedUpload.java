package com.aslmk.storageservice.dto;

import java.util.UUID;

public record InitChunkedUpload(UUID streamId, String filename) {}
