package com.satellitesystem.collisiondetection.service;

public class CollisionMath {

    private static final double EARTH_RADIUS = 6371.8;

    public static double[] latLonAltToXYZ(double latDeg, double lonDeg, double altKm) {
        double latRad = Math.toRadians(latDeg);
        double lonRad = Math.toRadians(lonDeg);
        double r = EARTH_RADIUS + altKm;

        return new double[]{
                r * Math.cos(latRad) * Math.cos(lonRad),
                r * Math.cos(latRad) * Math.sin(lonRad),
                r * Math.sin(latRad)
        };
    }

    public static double euclideanDistance(double[] pos1, double[] pos2) {
        double dx = pos2[0] - pos1[0];
        double dy = pos2[1] - pos1[1];
        double dz = pos2[2] - pos1[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}