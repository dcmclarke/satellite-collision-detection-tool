package com.satellitesystem.collisiondetection.propagation;

import java.time.Instant;

//a satellite's position (km) and velocity (km/s) at one instant, in the TEME frame that SGP4 works in
public record StateVector(
        Instant time,
        double x, double y, double z,
        double vx, double vy, double vz
) {
    //distance from the centre of the Earth, in km
    public double radiusKm() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
