package com.satellitesystem.collisiondetection.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollisionMathTest {

    private static final double TOLERANCE = 0.01;

    @Test
    void testLatLonAltToXYZ_AtEquatorPrimeMeridian() {
        double[] xyz = CollisionMath.latLonAltToXYZ(0.0, 0.0, 400.0);
        assertEquals(6771.8, xyz[0], TOLERANCE);
        assertEquals(0.0,    xyz[1], TOLERANCE);
        assertEquals(0.0,    xyz[2], TOLERANCE);
    }

    @Test
    void testEuclideanDistance_SamePoint() {
        double[] pos = CollisionMath.latLonAltToXYZ(51.0, -6.0, 400.0);
        assertEquals(0.0, CollisionMath.euclideanDistance(pos, pos), TOLERANCE);
    }

    @Test
    void testEuclideanDistance_AntipodalSatellites() {
        double[] northPole = CollisionMath.latLonAltToXYZ(90.0,  0.0, 400.0);
        double[] southPole = CollisionMath.latLonAltToXYZ(-90.0, 0.0, 400.0);
        double distance = CollisionMath.euclideanDistance(northPole, southPole);
        assertEquals(2 * 6771.8, distance, TOLERANCE);
    }

    @Test
    void testEuclideanDistance_VeryCloseSatellites() {
        double[] pos1 = CollisionMath.latLonAltToXYZ(0.0,  0.0,  400.0);
        double[] pos2 = CollisionMath.latLonAltToXYZ(0.01, 0.01, 400.0);
        double distance = CollisionMath.euclideanDistance(pos1, pos2);
        assertTrue(distance < 5.0, "Satellites 0.01° apart should be within 5km threshold");
    }
}