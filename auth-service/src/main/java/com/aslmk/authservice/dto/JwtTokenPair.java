package com.aslmk.authservice.dto;

public record JwtTokenPair(String accessToken, String refreshToken) {
}
