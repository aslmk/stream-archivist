package com.aslmk.authservice.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class TokenTimeUtil {

    public static LocalDateTime getExpiresAt(Integer exp, Clock clock) {
        if (exp == null) return LocalDateTime.now(clock).plusSeconds(60);

        if (isUnixTimestamp(exp)) return LocalDateTime
                .ofEpochSecond(exp, 0, ZoneOffset.UTC)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        return LocalDateTime.now(clock).plusSeconds(exp);
    }

    private static boolean isUnixTimestamp(Integer value) {
        return value > 100_000_000;
    }
}
