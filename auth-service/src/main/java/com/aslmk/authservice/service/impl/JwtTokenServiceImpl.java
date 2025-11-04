package com.aslmk.authservice.service.impl;

import com.aslmk.authservice.service.JwtTokenService;
import com.aslmk.authservice.util.RsaKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final RsaKeyProvider rsaKeys;

    @Value("${user.jwt.lifetime}")
    private Duration lifetime;

    public JwtTokenServiceImpl(RsaKeyProvider rsaKeys) {
        this.rsaKeys = rsaKeys;
    }

    @Override
    public String generate(String providerUserId, String providerName) {
        final long currentTime = new Date().getTime();
        return Jwts.builder()
                .subject(providerUserId)
                .signWith(rsaKeys.getPrivateKey(), Jwts.SIG.RS256)
                .claims(buildClaims(providerName))
                .issuedAt(new Date())
                .expiration(new Date(currentTime + lifetime.toMillis()))
                .compact();
    }

    private Claims buildClaims(String providerName) {
        return Jwts.claims()
                .add("provider_name", providerName)
                .build();
    }
}
