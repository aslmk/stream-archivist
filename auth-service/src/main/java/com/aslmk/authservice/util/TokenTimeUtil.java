package com.aslmk.authservice.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;

@Slf4j
public final class TokenTimeUtil {

    public static LocalDateTime getExpiresAt(Integer exp, Clock clock) {
        if (exp == null) {
            LocalDateTime defaultValue = LocalDateTime.now(clock).plusSeconds(60);
            log.debug("'expiresAt' field is null. Setting default value to {}", defaultValue);
            return defaultValue;
        }

        if (isUnixTimestamp(exp)) {
            LocalDateTime defaultValue = LocalDateTime
                    .ofEpochSecond(exp, 0, ZoneOffset.UTC);

            log.debug("'expiresAt' field is unix timestamp. Setting default value to {}", defaultValue);
            return defaultValue;
        }

        return LocalDateTime.now(clock).plusSeconds(exp);
    }

    private static boolean isUnixTimestamp(Integer value) {
        return value > 100_000_000;
    }
}
