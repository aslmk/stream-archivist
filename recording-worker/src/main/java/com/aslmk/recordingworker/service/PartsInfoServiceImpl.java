package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PartsInfoServiceImpl implements PartsInfoService {
    private final RecordingStorageProperties properties;
    private final Map<Path, Long> lastPartsInfoFileSize = new ConcurrentHashMap<>();
    private final WatchService watchService;

    public PartsInfoServiceImpl(RecordingStorageProperties properties) {
        this.properties = properties;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<String> watchForNewRecordedPart(String key) {
        try {
            Path path = getPartsInfoPath(key);
            WatchKey watchKey = watchService.poll(500, TimeUnit.MILLISECONDS);

            for (WatchEvent<?> event: watchKey.pollEvents()) {
                Path changed = (Path) event.context();
                if (changed.equals(path.getFileName())) {
                    return getLastRecordedPartName(changed);
                }
            }
            watchKey.reset();

            return Optional.empty();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLastRecordedPartIndex(String recordedPartName) {
        return parsePartIndex(recordedPartName);
    }

    @Override
    public Path getPartsInfoPath(String key) {
        try {
            String filename = key + "__parts_info.txt";
            Path path = Path.of(properties.getPath()).resolve(filename);

            if (!lastPartsInfoFileSize.containsKey(path)) {
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            }

            lastPartsInfoFileSize.putIfAbsent(path, 0L);
            return path;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFilePartPath(String key) {
        return Path.of(properties.getPath())
                .resolve(key + "_%08d.ts")
                .toString();
    }

    private Optional<String> getLastRecordedPartName(Path path) {
        try {
            long lastFileSize = lastPartsInfoFileSize.get(path);

            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
                raf.seek(lastFileSize);

                String lastFilePartName = null;
                String currentFilePartName;
                while ((currentFilePartName = raf.readLine()) != null) {
                    if (!currentFilePartName.isBlank()) lastFilePartName = currentFilePartName;
                }

                lastFileSize = raf.getFilePointer();
                lastPartsInfoFileSize.put(path, lastFileSize);
                return Optional.ofNullable(lastFilePartName);
            }

        } catch (IOException e) {
            log.error("Failed to get last recorded part name", e);
            throw new RuntimeException(e);
        }
    }

    private long parsePartIndex(String recordedPartName) {
        StringBuilder partIndex = new StringBuilder();

        // File parts saved in the format: 'streamer_username_%08d.ts'
        for (int currChar = recordedPartName.lastIndexOf("_") + 1;
             currChar < recordedPartName.length() - 1;
             currChar++) {
            if (recordedPartName.charAt(currChar) == '.') break;
            partIndex.append(recordedPartName.charAt(currChar));
        }

        try {
            return Long.parseLong(partIndex.toString());
        } catch (NumberFormatException e) {
            log.error("Failed to parse part index for '{}'", recordedPartName);
            throw new RuntimeException("Failed to parse part index", e);
        }
    }
}
