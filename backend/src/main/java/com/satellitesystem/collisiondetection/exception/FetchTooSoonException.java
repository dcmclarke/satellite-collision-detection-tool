package com.satellitesystem.collisiondetection.exception;

import java.time.Duration;

//thrown when Space-Track was contacted too recently, turned into a 429 by GlobalExceptionHandler
public class FetchTooSoonException extends RuntimeException {

    private final Duration retryAfter;

    public FetchTooSoonException(Duration retryAfter) {
        super("Space-Track data was fetched recently. To stay within Space-Track's usage limits, try again in "
                + Math.max(1, retryAfter.toMinutes()) + " minute(s). Load Backup Data works at any time.");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
