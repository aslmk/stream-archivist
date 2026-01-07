package com.aslmk.authservice.controller;

import com.aslmk.authservice.service.UserResolutionService;
import com.aslmk.common.dto.UserResolveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@Slf4j
public class UserController {

    private final UserResolutionService service;

    public UserController(UserResolutionService service) {
        this.service = service;
    }

    @GetMapping("/resolve")
    public UserResolveResponse resolve(@RequestParam String providerUserId,
                                       @RequestParam String providerName) {
        UUID userId = service.resolveUserId(providerUserId, providerName);
        return UserResolveResponse.builder()
                .userId(userId)
                .build();
    }
}
