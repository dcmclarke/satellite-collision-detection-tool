package com.satellitesystem.collisiondetection.dto;

import com.satellitesystem.collisiondetection.model.Alert;

import java.time.LocalDateTime;

//what the API sends back for an alert, kept separate from the JPA entity
public record AlertResponse(
        Long id,
        CollisionPredictionResponse prediction,
        String alertLevel,
        String message,
        LocalDateTime sentAt,
        boolean acknowledged
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getPrediction() == null ? null : CollisionPredictionResponse.from(alert.getPrediction()),
                alert.getAlertLevel(),
                alert.getMessage(),
                alert.getSentAt(),
                alert.isAcknowledged()
        );
    }
}
