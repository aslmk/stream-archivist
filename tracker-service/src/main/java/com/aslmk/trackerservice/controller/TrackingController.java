package com.aslmk.trackerservice.controller;

import com.aslmk.common.constants.GatewayHeaders;
import com.aslmk.trackerservice.dto.TrackingRequestDto;
import com.aslmk.trackerservice.dto.UserInfoDto;
import com.aslmk.trackerservice.service.TrackingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/streamers")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/track")
    public ResponseEntity<Void> trackStreamer(HttpServletRequest request,
                                      @RequestBody TrackingRequestDto trackingRequest) {
        UserInfoDto userInfo = UserInfoDto.builder()
                .providerUserId(request.getHeader(GatewayHeaders.USER_ID))
                .providerName(request.getHeader(GatewayHeaders.PROVIDER_NAME))
                .build();

        trackingService.trackStreamer(userInfo, trackingRequest);

        return ResponseEntity.ok().build();
    }
}
