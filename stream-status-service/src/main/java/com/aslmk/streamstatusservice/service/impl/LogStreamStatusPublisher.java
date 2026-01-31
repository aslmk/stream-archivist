package com.aslmk.streamstatusservice.service.impl;

import com.aslmk.streamstatusservice.entity.StreamStatusEntity;
import com.aslmk.streamstatusservice.service.StreamStatusPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogStreamStatusPublisher implements StreamStatusPublisher {
    @Override
    public void publish(StreamStatusEntity status) {
        log.info("Stream status updated: RecordingStatus={}, StreamIsLive={}, streamerId={}", status.getRecordingStatus(), status.isLive(), status.getStreamerId());
    }
}
