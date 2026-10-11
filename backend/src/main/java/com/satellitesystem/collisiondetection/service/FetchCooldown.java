package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.FetchTooSoonException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Limits how often the app contacts Space-Track. Its API guidelines ask for GP data at most once an hour,
 * and accounts that exceed the limits can be suspended.
 * Every attempt counts, including failed ones, because repeated failed logins are also a risk to the account.
 * The time is kept in memory, so restarting the app resets it.
 */
@Component
public class FetchCooldown {

    private final Duration minInterval;
    private final Clock clock;
    private Instant lastAttempt; //null until the first fetch

    //two constructors, so @Autowired tells Spring which one to use
    @Autowired
    public FetchCooldown(@Value("${spacetrack.api.min-fetch-interval:1h}") Duration minInterval) {
        this(minInterval, Clock.systemUTC());
    }

    //lets tests control the time
    FetchCooldown(Duration minInterval, Clock clock) {
        this.minInterval = minInterval;
        this.clock = clock;
    }

    //records a fetch attempt, or throws FetchTooSoonException if the last one was too recent
    //synchronized so two requests arriving together can't both get through
    public synchronized void acquire() {
        Instant now = clock.instant();
        if (lastAttempt != null) {
            Instant nextAllowed = lastAttempt.plus(minInterval);
            if (now.isBefore(nextAllowed)) {
                throw new FetchTooSoonException(Duration.between(now, nextAllowed));
            }
        }
        lastAttempt = now;
    }
}
