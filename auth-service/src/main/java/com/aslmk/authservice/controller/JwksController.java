package com.aslmk.authservice.controller;

import com.aslmk.authservice.util.RsaKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    private final RsaKeyProvider rsaKeys;

    public JwksController(RsaKeyProvider rsaKeys) {
        this.rsaKeys = rsaKeys;
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeys.getPublicKey();

        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("n", Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));
        jwk.put("kid", "auth-service-key-1");

        return Map.of("keys", List.of(jwk));
    }
}
