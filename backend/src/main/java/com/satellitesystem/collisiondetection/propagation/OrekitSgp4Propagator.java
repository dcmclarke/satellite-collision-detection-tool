package com.satellitesystem.collisiondetection.propagation;

import org.orekit.data.ClasspathCrawler;
import org.orekit.data.DataContext;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;

import java.time.Instant;

/**
 * SGP4/SDP4 propagation using the Orekit library (Apache 2.0).
 * Orekit picks SGP4 for near-Earth orbits and SDP4 for deep-space ones (period of 225 minutes or more).
 * Results are in the TEME frame, in km and km/s.
 */
public class OrekitSgp4Propagator implements OrbitPropagator {

    static {
        //Orekit needs the leap-second table to convert between UTC and its internal time scale
        //tai-utc.dat is the public USNO file, bundled so the app doesn't need the full orekit-data download
        DataContext.getDefault().getDataProvidersManager()
                .addProvider(new ClasspathCrawler("orekit-data/tai-utc.dat"));
    }

    private final TimeScale utc = TimeScalesFactory.getUTC();

    @Override
    public StateVector propagate(TwoLineElementSet tle, Instant time) {
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(parse(tle));
        PVCoordinates pv = propagator.getPVCoordinates(toAbsoluteDate(time), propagator.getFrame());

        //Orekit uses metres, the rest of the app uses km
        return new StateVector(time,
                pv.getPosition().getX() / 1000, pv.getPosition().getY() / 1000, pv.getPosition().getZ() / 1000,
                pv.getVelocity().getX() / 1000, pv.getVelocity().getY() / 1000, pv.getVelocity().getZ() / 1000);
    }

    @Override
    public Instant epochOf(TwoLineElementSet tle) {
        return parse(tle).getDate().toInstant();
    }

    //the TLE constructor doesn't verify checksums, so check first: a corrupted TLE should fail, not give a wrong position
    //isFormatOK throws an OrekitException for a bad checksum and returns false for a malformed line
    private TLE parse(TwoLineElementSet tle) {
        if (!TLE.isFormatOK(tle.line1(), tle.line2())) {
            throw new IllegalArgumentException("Invalid TLE format: " + tle.line1());
        }
        return new TLE(tle.line1(), tle.line2());
    }

    //converts directly from Instant, keeping full precision (going through java.util.Date would round to milliseconds)
    private AbsoluteDate toAbsoluteDate(Instant time) {
        return new AbsoluteDate(time, utc);
    }
}
