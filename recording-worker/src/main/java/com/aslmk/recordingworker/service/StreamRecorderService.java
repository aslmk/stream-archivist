package com.aslmk.recordingworker.service;

import com.aslmk.common.dto.RecordingRequestDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StreamRecorderService {

    public void recordStream(RecordingRequestDto request) {
        ProcessBuilder pb = getProcessBuilder(request);

        try {
            Process process = pb.start();

            readOutput(process.getInputStream());

            int exitValue = process.waitFor();

            if (exitValue != 0) {
                System.out.println("Process exited with code " + exitValue);
            } else {
                System.out.printf("'%s' stream was recorded successfully%n",
                        request.getStreamerUsername());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static ProcessBuilder getProcessBuilder(RecordingRequestDto request) {
        String videoOutputName = getCurrentDateTime() + "_" +
                request.getStreamerUsername()+".mp4";

        String currentDir = Paths.get("").toAbsolutePath().toString();

        List<String> command = List.of("docker", "run", "--rm", "-v",
                 "\"" + currentDir + "/recordings:/recordings\"", "streamlink-runner",
                request.getStreamUrl(),
                request.getStreamQuality(), "-o", "/recordings/"+videoOutputName);

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        return pb;
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
