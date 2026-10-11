package com.satellitesystem.collisiondetection.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

//starts the full app with the scheduled refresh switched on, to check the job and its @Scheduled settings load
//the first run is 7 minutes after startup, so it never actually contacts Space-Track during the test
@SpringBootTest(properties = "spacetrack.ingest.enabled=true")
class SpaceTrackIngestJobWiringTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void ingestJob_IsCreatedWhenEnabled() {
        assertEquals(1, context.getBeansOfType(SpaceTrackIngestJob.class).size());
    }
}
