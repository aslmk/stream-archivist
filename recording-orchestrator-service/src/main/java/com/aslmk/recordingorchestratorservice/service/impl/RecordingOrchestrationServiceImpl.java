package com.aslmk.recordingorchestratorservice.service.impl;

import com.aslmk.common.dto.StreamLifecycleEvent;
import com.aslmk.recordingorchestratorservice.rabbitmq.RabbitMqService;
import com.aslmk.recordingorchestratorservice.service.RecordingOrchestrationService;
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
    public void processRecordingRequest(StreamLifecycleEvent streamLifecycleEvent) {
        log.info("Orchestrating recording request: streamerUsername={}, streamUrl={}",
                streamLifecycleEvent.getStreamerUsername(),
                streamLifecycleEvent.getStreamUrl()
        );
        rabbitMqService.sendMessage(streamLifecycleEvent);
    }
}
