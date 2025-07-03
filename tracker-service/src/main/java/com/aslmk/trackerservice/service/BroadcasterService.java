package com.aslmk.trackerservice.service;

import com.aslmk.trackerservice.dto.AddBroadcasterRequestDto;
import com.aslmk.trackerservice.dto.DeleteBroadcasterRequestDto;

public interface BroadcasterService {
    void saveBroadcaster(AddBroadcasterRequestDto newBroadcaster);
    void deleteBroadcaster(DeleteBroadcasterRequestDto deleteBroadcaster);
}
