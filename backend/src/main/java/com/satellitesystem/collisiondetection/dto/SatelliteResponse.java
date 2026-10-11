package com.satellitesystem.collisiondetection.dto;

import com.satellitesystem.collisiondetection.model.Satellite;

//what the API sends back for a satellite, kept separate from the JPA entity
public record SatelliteResponse(
        Long id,
        String name,
        String noradId,
        double latitude,
        double longitude,
        double altitude
) {
    public static SatelliteResponse from(Satellite satellite) {
        return new SatelliteResponse(
                satellite.getId(),
                satellite.getName(),
                satellite.getNoradId(),
                satellite.getLatitude(),
                satellite.getLongitude(),
                satellite.getAltitude()
        );
    }
}
