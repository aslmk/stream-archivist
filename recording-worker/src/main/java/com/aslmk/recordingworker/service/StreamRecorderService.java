package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordingRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StreamRecorderService {

    public void recordStream(RecordingRequestDto request) {
        ProcessBuilder pb = getProcessBuilder(request);

        try {
            Process process = pb.start();
            readOutput(process.getInputStream());

            int exitCode = process.waitFor();
            handleExitCode(exitCode, request.getStreamerUsername());

        } catch (IOException e) {
            log.error("Failed to record stream", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Recording thread interrupted", e);
        }
    }

    private static void handleExitCode(int exitCode, String streamerUsername) {
        if (exitCode != 0) {
            log.warn("Process exited with code {}", exitCode);
        } else {
            log.info("'{}' stream was recorded successfully", streamerUsername);
        }
    }

    private static ProcessBuilder getProcessBuilder(RecordingRequestDto request) {
        String videoOutputName = getVideoOutputName(request.getStreamerUsername());
        String currentDir = getCurrentDirectoryPath();

        List<String> command = getCommand(request, videoOutputName, currentDir);

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        return pb;
    }

    private static List<String> getCommand(RecordingRequestDto request,
                                           String videoOutputName,
                                           String currentDir) {
        String command = String.format(
                "streamlink -O %s %s | ffmpeg -i - -c copy -ss 15 /recordings/%s",
                request.getStreamUrl(),
                request.getStreamQuality(),
                videoOutputName);

        return List.of(
                "docker", "run", "--rm", "-v",
                 currentDir + "/recordings:/recordings",
                "streamlink-ffmpeg-runner",
                "bash", "-c", command);
    }

    private static String getCurrentDirectoryPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    private static String getVideoOutputName(String streamerUsername) {
        return  getCurrentDateTime() + "_" + streamerUsername +".mp4";
    }

    private static String getCurrentDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        return DateTimeFormatter.ofPattern("dd_MM_yyyy").format(dateTime);
    }

    private static void readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(System.out::println);
        }
    }
}
