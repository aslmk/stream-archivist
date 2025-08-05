package com.aslmk.trackerservice.service.impl;

import com.aslmk.trackerservice.dto.AddStreamerRequestDto;
import com.aslmk.trackerservice.dto.DeleteStreamerRequestDto;
import com.aslmk.trackerservice.entity.Streamer;
import com.aslmk.trackerservice.entity.StreamingPlatform;
import com.aslmk.trackerservice.exception.StreamerNotFoundException;
import com.aslmk.trackerservice.repository.StreamerRepository;
import com.aslmk.trackerservice.service.StreamerService;
import org.springframework.stereotype.Service;

@Service
public class StreamerServiceImpl implements StreamerService {
    private final StreamerRepository streamerRepository;

    public StreamerServiceImpl(StreamerRepository streamerRepository) {
        this.streamerRepository = streamerRepository;
    }

    @Override
    public void saveStreamer(AddStreamerRequestDto newStreamer) {
        Streamer streamer = Streamer.builder()
                .username(newStreamer.getUsername())
                .platform(StreamingPlatform.fromString(newStreamer.getPlatform()))
                .build();
        streamerRepository.save(streamer);
    }

    @Override
    public void deleteStreamer(DeleteStreamerRequestDto deleteStreamer) {
        Streamer streamer = streamerRepository.findByUsernameAndPlatform(
                deleteStreamer.getUsername(),
                StreamingPlatform.fromString(deleteStreamer.getPlatform())
        ).orElseThrow(
                () -> new StreamerNotFoundException(
                String.format("Streamer '%s' on '%s' not found",
                        deleteStreamer.getUsername(),
                        deleteStreamer.getPlatform())
        ));

        streamerRepository.delete(streamer);
    }
}
