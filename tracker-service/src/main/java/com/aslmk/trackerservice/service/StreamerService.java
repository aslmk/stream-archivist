package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.AddStreamerRequestDto;
import com.aslmk.trackerservice.dto.DeleteStreamerRequestDto;

public interface StreamerService {
    void saveStreamer(AddStreamerRequestDto newBroadcaster);
    void deleteStreamer(DeleteStreamerRequestDto deleteBroadcaster);
}
