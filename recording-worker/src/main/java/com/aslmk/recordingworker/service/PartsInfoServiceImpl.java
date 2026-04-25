package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.config.RecordingStorageProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PartsInfoServiceImpl implements PartsInfoService {
    private final RecordingStorageProperties properties;
    private final Map<String, Queue<String>> pendingFileParts = new ConcurrentHashMap<>();
    private final WatchService watchService;

    @PreDestroy
    public void destroy() throws IOException {
        watchService.close();
    }

    public PartsInfoServiceImpl(RecordingStorageProperties properties) {
        this.properties = properties;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            Path path = Path.of(properties.getPath());
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<String> watchForNewRecordedPart(String key) {
        try {
            WatchKey watchKey = watchService.poll(500, TimeUnit.MILLISECONDS);

            if (watchKey != null) {
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    Path newFilePart = (Path) event.context();
                    String newFilePartName = newFilePart.getFileName().toString();

                    if (!newFilePartName.endsWith(".ts")) continue;

                    String partOwner = getPartName(newFilePartName);

                    pendingFileParts.computeIfAbsent(partOwner, f -> new ConcurrentLinkedQueue<>())
                            .add(newFilePartName);
                }
                watchKey.reset();
            }

            Queue<String> queue = pendingFileParts.get(key);
            return Optional.ofNullable(queue != null ? queue.poll() : null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getRecordedPartIndex(String recordedPartName) {
        return parsePartIndex(recordedPartName);
    }

    @Override
    public Path getPartsInfoPath(String key) {
        String filename = key + "__parts_info.txt";
        return Path.of(properties.getPath()).resolve(filename);
    }

    @Override
    public String getFilePartPath(String key) {
        return Path.of(properties.getPath())
                .resolve(key + "_%08d.ts")
                .toString();
    }

    @Override
    public boolean isPartsInfoExists(String key) {
        Path path = getPartsInfoPath(key);
        return Files.exists(path);
    }

    @Override
    public List<String> getRecordedParts(String key) {
        Path path = getPartsInfoPath(key);
        List<String> lastRecordedParts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {

            String currentFilePartName;
            while ((currentFilePartName = br.readLine()) != null) {
                lastRecordedParts.add(currentFilePartName);
            }

            return lastRecordedParts;
        } catch (IOException e) {
            log.error("Failed to get recorded parts for '{}'", path.getFileName());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> getLastRecordedPartName(Path path) {
        if (!Files.exists(path)) return Optional.empty();

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String lastRecordedPartName = null;
            String currentFilePartName;
            while ((currentFilePartName = br.readLine()) != null) {
                lastRecordedPartName = currentFilePartName;
            }
            return Optional.ofNullable(lastRecordedPartName);
        } catch (IOException e) {
            log.error("Failed to get last recorded part for '{}'", path.getFileName());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearPendingFileParts(String key) {
        pendingFileParts.remove(key);
    }

    private long parsePartIndex(String recordedPartName) {
        String indexString = Arrays.stream(recordedPartName.split("_")).toList().getLast();
        String partIndex = indexString.substring(0, indexString.indexOf("."));
        try {
            return Long.parseLong(partIndex);
        } catch (NumberFormatException e) {
            log.error("Failed to parse part index for '{}'. Returning default '0'", recordedPartName);
            return 0L;
        }
    }

    private String getPartName(String recordedPartName) {
        return recordedPartName.substring(0, recordedPartName.lastIndexOf("_"));
    }
}
