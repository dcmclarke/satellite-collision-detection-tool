package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.FetchTooSoonException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FetchCooldownTest {

    private final TestClock clock = new TestClock();
    private final FetchCooldown cooldown = new FetchCooldown(Duration.ofHours(1), clock);

    @Test
    void firstFetch_IsAllowed() {
        assertDoesNotThrow(cooldown::acquire);
    }

    @Test
    void secondFetchWithinInterval_IsRejectedWithTimeLeft() {
        cooldown.acquire();
        clock.advance(Duration.ofMinutes(20));

        FetchTooSoonException e = assertThrows(FetchTooSoonException.class, cooldown::acquire);

        assertEquals(Duration.ofMinutes(40), e.getRetryAfter());
    }

    @Test
    void fetchAfterInterval_IsAllowed() {
        cooldown.acquire();
        clock.advance(Duration.ofHours(1));

        assertDoesNotThrow(cooldown::acquire);
    }

    @Test
    void rejectedFetch_DoesNotResetTheTimer() {
        cooldown.acquire();
        clock.advance(Duration.ofMinutes(50));
        assertThrows(FetchTooSoonException.class, cooldown::acquire);

        //the rejected attempt at 50 minutes didn't restart the hour, so 60 minutes after the first fetch is allowed
        clock.advance(Duration.ofMinutes(10));
        assertDoesNotThrow(cooldown::acquire);
    }

    //a clock the test can move forward, instead of waiting for real time to pass
    private static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
