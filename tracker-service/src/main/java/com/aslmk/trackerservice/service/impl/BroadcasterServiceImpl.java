package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.AddBroadcasterRequestDto;
import com.aslmk.trackerservice.dto.DeleteBroadcasterRequestDto;
import com.aslmk.trackerservice.entity.Broadcaster;
import com.aslmk.trackerservice.entity.StreamingPlatform;
import com.aslmk.trackerservice.exception.BroadcasterNotFoundException;
import com.aslmk.trackerservice.repository.BroadcasterRepository;
import com.aslmk.trackerservice.service.BroadcasterService;
import org.springframework.stereotype.Service;

@Service
public class BroadcasterServiceImpl implements BroadcasterService {
    private final BroadcasterRepository broadcasterRepository;

    public BroadcasterServiceImpl(BroadcasterRepository broadcasterRepository) {
        this.broadcasterRepository = broadcasterRepository;
    }

    @Override
    public void saveBroadcaster(AddBroadcasterRequestDto newBroadcaster) {
        Broadcaster broadcaster = Broadcaster.builder()
                .username(newBroadcaster.getUsername())
                .platform(StreamingPlatform.fromString(newBroadcaster.getPlatform()))
                .build();
        broadcasterRepository.save(broadcaster);
    }

    @Override
    public void deleteBroadcaster(DeleteBroadcasterRequestDto deleteBroadcaster) {
        Broadcaster broadcaster = broadcasterRepository.findByUsernameAndPlatform(
                deleteBroadcaster.getUsername(),
                StreamingPlatform.fromString(deleteBroadcaster.getPlatform())
        ).orElseThrow(
                () -> new BroadcasterNotFoundException(
                String.format("Broadcaster '%s' on '%s' not found",
                        deleteBroadcaster.getUsername(),
                        deleteBroadcaster.getPlatform())
        ));

        broadcasterRepository.delete(broadcaster);
    }
}
