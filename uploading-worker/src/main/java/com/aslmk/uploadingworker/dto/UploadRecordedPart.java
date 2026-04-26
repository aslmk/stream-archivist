package com.aslmk.uploadingworker.dto;

import java.nio.file.Path;

public record UploadRecordedPart(Path filePath, PreSignedUrl preSignedUrl) {}
