package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.service.JwtTokenService;
import com.aslmk.authservice.util.RsaKeyProvider;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final RsaKeyProvider rsaKeys;

    @Value("${user.jwt.lifetime}")
    private Duration lifetime;

    public JwtTokenServiceImpl(RsaKeyProvider rsaKeys) {
        this.rsaKeys = rsaKeys;
    }

    @Override
    public String generate(UUID userId) {
        final long currentTime = new Date().getTime();
        return Jwts.builder()
                .subject(userId.toString())
                .signWith(rsaKeys.getPrivateKey(), Jwts.SIG.RS256)
                .issuedAt(new Date())
                .expiration(new Date(currentTime + lifetime.toMillis()))
                .compact();
    }
}
