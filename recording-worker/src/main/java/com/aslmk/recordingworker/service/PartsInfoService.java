package com.aslmk.recordingworker.service;

import java.nio.file.Path;
import java.util.Optional;

public interface PartsInfoService {
    long getLastRecordedPartIndex(String recordedPartName);
    Optional<String> watchForNewRecordedPart(String key);
    String getFilePartPath(String key);
    Path getPartsInfoPath(String key);
}
