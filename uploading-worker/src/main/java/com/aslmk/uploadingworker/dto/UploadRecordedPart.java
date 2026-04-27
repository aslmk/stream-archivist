package com.aslmk.uploadingworker.dto;

import java.nio.file.Path;

@Deprecated
public record UploadRecordedPart(Path filePath, PreSignedUrl preSignedUrl) {}
