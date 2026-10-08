package com.satellitesystem.collisiondetection.exception;

//thrown when a record asked for by id doesn't exist, turned into a 404 by GlobalExceptionHandler
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
