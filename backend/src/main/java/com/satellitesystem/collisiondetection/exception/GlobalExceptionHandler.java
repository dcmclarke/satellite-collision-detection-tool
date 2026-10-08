package com.satellitesystem.collisiondetection.exception;

import com.satellitesystem.collisiondetection.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//turns exceptions thrown by any controller into an HTTP status and a JSON error body
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    //502 Bad Gateway: our server is fine, but the upstream server it depends on failed
    @ExceptionHandler(SpaceTrackException.class)
    public ResponseEntity<ErrorResponse> handleSpaceTrackFailure(SpaceTrackException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(e.getMessage()));
    }
}
