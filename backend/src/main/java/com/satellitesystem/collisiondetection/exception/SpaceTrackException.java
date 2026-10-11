package com.satellitesystem.collisiondetection.exception;

//thrown when the Space-Track.org API can't be reached or returns an error, turned into a 502 by GlobalExceptionHandler
public class SpaceTrackException extends RuntimeException {

    public SpaceTrackException(String message) {
        super(message);
    }

    public SpaceTrackException(String message, Throwable cause) {
        super(message, cause);
    }
}
