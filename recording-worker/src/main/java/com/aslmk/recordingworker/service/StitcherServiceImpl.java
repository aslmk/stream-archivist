package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import com.aslmk.recordingworker.exception.StitchingServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Slf4j
@Service
public class StitcherServiceImpl implements StitcherService {

    private final RecordingStorageProperties properties;
    private final ProcessExecutor processExecutor;

    public StitcherServiceImpl(RecordingStorageProperties properties,
                               ProcessExecutor processExecutor) {
        this.properties = properties;
        this.processExecutor = processExecutor;
    }

    @Override
    public void init(String key) {
        try {
            Path stitchingFilePath = getStitchingFilePath(key);

            if (Files.exists(stitchingFilePath)) return;

            Files.createFile(stitchingFilePath);
        } catch (FileAlreadyExistsException e) {
            log.warn("File with the key '{}' is already exists", key);
        } catch (IOException e) {
            throw new StitchingServiceException("Failed to create a file required for stitching", e);
        }
    }

    @Override
    public void append(String key, String data) {
        try {
            Path stitchingFilePath = getStitchingFilePath(key);

            if (!Files.exists(stitchingFilePath)) {
                throw new StitchingServiceException("File required for stitching does not exist!");
            }

            String row = String.format("file '%s'", data);
            Files.write(stitchingFilePath,
                    List.of(row),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new StitchingServiceException("Failed to append data for stitching", e);
        }
    }

    @Override
    public void stitch(String key, String fileOutputName) {
        Path stitchingFilePath = getStitchingFilePath(key);
        Path outputFilePath = getFilePath(fileOutputName);

        if (!Files.exists(stitchingFilePath)) {
            throw new StitchingServiceException("File required for stitching does not exist!");
        }

        List<String> command = List.of("ffmpeg", "-f", "concat", "-safe", "0",
                "-i", stitchingFilePath.toString(), "-c", "copy", "-fflags", "+genpts+igndts",
                "-movflags", "+faststart", outputFilePath.toString());

        boolean result = processExecutor.execute(command);

        if (result) {
            log.debug("Successfully stitched recorded parts: fileOutputName='{}'", fileOutputName);
        } else {
            throw new StitchingServiceException("Failed to stitch recorded parts");
        }
    }

    private Path getStitchingFilePath(String key) {
        String filename = key + "__final.txt";
        return getFilePath(filename);
    }

    private Path getFilePath(String filename) {
        return Path.of(properties.getPath()).resolve(filename);
    }
}