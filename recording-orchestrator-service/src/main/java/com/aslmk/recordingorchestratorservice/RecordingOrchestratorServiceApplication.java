package com.aslmk.recordingorchestratorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RecordingOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecordingOrchestratorServiceApplication.class, args);
    }

}
