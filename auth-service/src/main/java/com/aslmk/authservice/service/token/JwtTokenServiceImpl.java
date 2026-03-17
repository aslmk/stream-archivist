package com.aslmk.authservice.service.token;

import com.aslmk.authservice.config.RsaKeyProvider;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private final RsaKeyProvider rsaKeys;

    @Value("${user.jwt-access-token.lifetime}")
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
