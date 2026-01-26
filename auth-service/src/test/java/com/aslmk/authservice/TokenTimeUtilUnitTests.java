package com.aslmk.authservice;

import com.aslmk.authservice.util.TokenTimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TokenTimeUtilUnitTests {

    private static final ZonedDateTime NOW = ZonedDateTime.of(
            2025, 10, 23,
            19, 0, 0, 0,
            ZoneId.of("UTC")
    );

    private static final int NOW_INTEGER_VALUE = 1_761_228_000; // 2025-10-23T19:00

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW.toInstant(), NOW.getZone());
    }

    @Test
    void getExpiresAt_should_useDefault60Seconds_when_expIsMissing() {
        LocalDateTime result = TokenTimeUtil.getExpiresAt(null, clock);

        Assertions.assertEquals(
                NOW.plusMinutes(1).toInstant(),
                result.atZone(ZoneId.of("UTC")).toInstant()
        );
    }

    @Test
    void getExpiresAt_should_addExpSeconds_when_expRepresentsTTL() {
        LocalDateTime result = TokenTimeUtil.getExpiresAt(900, clock); // 15 minutes

        Assertions.assertEquals(
                NOW.plusSeconds(900).toInstant(),
                result.atZone(ZoneId.of("UTC")).toInstant()
        );
    }

    @Test
    void getExpiresAt_should_useExactEpochTime_when_expIsFullTimestamp() {
        LocalDateTime result = TokenTimeUtil.getExpiresAt(NOW_INTEGER_VALUE, clock);

        Assertions.assertEquals(
                NOW.toInstant(),
                result.atZone(ZoneId.of("UTC")).toInstant()
        );
    }
}
