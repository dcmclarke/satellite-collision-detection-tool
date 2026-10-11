package com.satellitesystem.collisiondetection.dto;

//JSON body returned when a request fails, e.g. {"message": "Satellite not found: 42"}
public record ErrorResponse(String message) {
}
