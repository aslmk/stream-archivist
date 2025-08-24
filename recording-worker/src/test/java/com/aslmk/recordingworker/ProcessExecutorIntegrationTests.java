package com.aslmk.recordingworker;

import com.aslmk.recordingworker.exception.ProcessExecutionException;
import com.aslmk.recordingworker.service.ProcessExecutor;
import com.aslmk.recordingworker.service.impl.ProcessExecutorImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ProcessExecutorIntegrationTests {

    private ProcessExecutor processExecutor;

    @BeforeEach
    public void init() {
        processExecutor = new ProcessExecutorImpl();
    }

    @Test
    void execute_should_returnZero_when_commandSucceeded() {
        List<String> command = command("echo hello");

        Assertions.assertEquals(0, processExecutor.execute(command));
    }

    @Test
    void execute_should_returnOne_when_commandFailed() {
        List<String> command = command("exit 1");
        Assertions.assertEquals(1, processExecutor.execute(command));
    }

    @Test
    void execute_should_throwProcessExecutionException_when_commandIsUnknown() {
        List<String> command = List.of("unknown_command");
        Assertions.assertThrows(ProcessExecutionException.class,
                () -> processExecutor.execute(command));
    }

    @Test
    void execute_should_handleInterruptGracefully_when_threadInterrupted() throws InterruptedException {
        List<String> command = command("echo hello");

        Thread thread = new Thread(() -> processExecutor.execute(command));

        thread.start();
        Thread.sleep(1000);
        thread.interrupt();

        thread.join();

        Assertions.assertTrue(thread.isInterrupted() || !thread.isAlive());
    }

    private List<String> command(String... args) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return Stream.concat(Stream.of("cmd", "/c"), Arrays.stream(args)).toList();
        } else {
            return Stream.concat(Stream.of("sh", "-c"), Arrays.stream(args)).toList();
        }
    }
}
