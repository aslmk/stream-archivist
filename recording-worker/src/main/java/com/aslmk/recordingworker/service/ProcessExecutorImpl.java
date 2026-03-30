package com.aslmk.recordingworker.service;

import com.aslmk.recordingworker.exception.ProcessExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Component
public class ProcessExecutorImpl implements ProcessExecutor {

    private static final int MAX_RETRIES = 4;

    @Override
    public boolean execute(List<String> command) {
        try {
            log.info("Executing process: {}", String.join(" ", command));
            ProcessBuilder pb = getProcessBuilder(command);

            int attempts = 0;

            while (attempts < MAX_RETRIES) {
                Process process = pb.start();
                readOutput(process.getInputStream());
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.debug("Attempt '{}' to record stream failed with exit code '{}'", attempts, exitCode);
                    attempts++;
                } else {
                    return true;
                }

            }

            log.info("Failed to execute process: {}", String.join(" ", command));
            return false;
        } catch (IOException e) {
            log.error("Failed to start process. Command={}", String.join(" ", command), e);
            throw new ProcessExecutionException("Failed to start process: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Process execution was interrupted. Command={}", String.join(" ", command), e);
            throw new ProcessExecutionException("Recording thread interrupted: " + e.getMessage());
        }
    }

    private ProcessBuilder getProcessBuilder(List<String> command) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(command);

        return pb;
    }

    private void readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(log::debug);
        }
    }
}