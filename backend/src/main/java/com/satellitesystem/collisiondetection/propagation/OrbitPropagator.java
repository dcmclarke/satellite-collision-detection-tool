package com.satellitesystem.collisiondetection.propagation;

import java.time.Instant;

//works out where a satellite is at a given time from its orbital elements
//an interface so the rest of the app doesn't depend on Orekit directly, and tests can use a fake
public interface OrbitPropagator {

    StateVector propagate(TwoLineElementSet tle, Instant time);

    //the time the elements were measured, which is when the prediction is most accurate
    Instant epochOf(TwoLineElementSet tle);
}
