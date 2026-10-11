package com.satellitesystem.collisiondetection.propagation;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//checks the propagator against the published SGP4 verification case for satellite 00005 (Vanguard 1)
//from Vallado et al., "Revisiting Spacetrack Report #3" (AIAA 2006-6753), the standard reference for SGP4 implementations
class OrekitSgp4PropagatorTest {

    private static final TwoLineElementSet VANGUARD_1 = new TwoLineElementSet(
            "1 00005U 58002B   00179.78495062  .00000023  00000-0  28098-4 0  4753",
            "2 00005  34.2682 348.7242 1859667 331.7664  19.3264 10.82419157413667");

    //1 m and 1 mm/s: far tighter than the app needs, with room for floating-point differences between implementations
    private static final double POSITION_TOLERANCE_KM = 0.001;
    private static final double VELOCITY_TOLERANCE_KM_S = 0.000001;

    private final OrbitPropagator propagator = new OrekitSgp4Propagator();

    @Test
    void propagate_AtEpoch_MatchesVerificationCase() {
        Instant epoch = propagator.epochOf(VANGUARD_1);

        StateVector state = propagator.propagate(VANGUARD_1, epoch);

        assertPosition(state, 7022.46529266, -1400.08296755, 0.03995155);
        assertVelocity(state, 1.893841015, 6.405893759, 4.534807250);
    }

    @Test
    void propagate_SixHoursAfterEpoch_MatchesVerificationCase() {
        Instant sixHoursLater = propagator.epochOf(VANGUARD_1).plus(Duration.ofMinutes(360));

        StateVector state = propagator.propagate(VANGUARD_1, sixHoursLater);

        assertPosition(state, -7154.03120202, -3783.17682504, -3536.19412294);
        assertVelocity(state, 4.741887409, -4.151817765, -2.093935425);
    }

    @Test
    void propagate_CorruptedTle_IsRejected() {
        //last digit of line 2 is the checksum; changing it makes the line invalid
        TwoLineElementSet corrupted = new TwoLineElementSet(VANGUARD_1.line1(),
                VANGUARD_1.line2().substring(0, 68) + "0");

        assertThrows(RuntimeException.class, () -> propagator.propagate(corrupted, Instant.now()));
    }

    private void assertPosition(StateVector state, double x, double y, double z) {
        assertEquals(x, state.x(), POSITION_TOLERANCE_KM);
        assertEquals(y, state.y(), POSITION_TOLERANCE_KM);
        assertEquals(z, state.z(), POSITION_TOLERANCE_KM);
    }

    private void assertVelocity(StateVector state, double vx, double vy, double vz) {
        assertEquals(vx, state.vx(), VELOCITY_TOLERANCE_KM_S);
        assertEquals(vy, state.vy(), VELOCITY_TOLERANCE_KM_S);
        assertEquals(vz, state.vz(), VELOCITY_TOLERANCE_KM_S);
    }
}
