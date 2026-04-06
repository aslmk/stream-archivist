package com.aslmk.recordingorchestratorservice.service;

import com.aslmk.recordingorchestratorservice.dto.RecordingStatusEvent;
import com.aslmk.recordingorchestratorservice.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.messaging.rabbitmq.RabbitMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordingOrchestrationServiceImpl implements RecordingOrchestrationService {

    private final RabbitMqService rabbitMqService;

    public RecordingOrchestrationServiceImpl(RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
    }

    @Override
    public void processStreamEvent(StreamLifecycleEvent event) {
        rabbitMqService.sendMessage(event);
    }

    @Override
    public void processRecordingEvent(RecordingStatusEvent event) {
        rabbitMqService.sendMessage(event);
    }
}
