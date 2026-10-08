package com.satellitesystem.collisiondetection.dto;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;

import java.time.LocalDateTime;

//what the API sends back for a collision prediction, kept separate from the JPA entity
public record CollisionPredictionResponse(
        Long id,
        SatelliteResponse satellite1,
        SatelliteResponse satellite2,
        LocalDateTime predictedTime,
        double minimumDistance,
        int probabilityScore,
        String riskLevel,
        String status,
        LocalDateTime createdAt
) {
    public static CollisionPredictionResponse from(CollisionPrediction prediction) {
        return new CollisionPredictionResponse(
                prediction.getId(),
                SatelliteResponse.from(prediction.getSatellite1()),
                SatelliteResponse.from(prediction.getSatellite2()),
                prediction.getPredictedTime(),
                prediction.getMinimumDistance(),
                prediction.getProbabilityScore(),
                prediction.getRiskLevel(),
                prediction.getStatus(),
                prediction.getCreatedAt()
        );
    }
}
