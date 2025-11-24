package com.aslmk.recordingworker.service.impl;

import com.aslmk.recordingworker.exception.ProcessExecutionException;
import com.aslmk.recordingworker.service.ProcessExecutor;
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

    @Override
    public int execute(List<String> command) {
        log.info("Executing process: {}", String.join(" ", command));

        ProcessBuilder pb = getProcessBuilder(command);

        try {
            Process process = pb.start();
            readOutput(process.getInputStream());

            return process.waitFor();
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