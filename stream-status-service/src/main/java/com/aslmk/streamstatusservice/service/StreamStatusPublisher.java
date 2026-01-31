package com.aslmk.streamstatusservice.service;

import com.aslmk.streamstatusservice.entity.StreamStatusEntity;

public interface StreamStatusPublisher {
    void publish(StreamStatusEntity status);
}
