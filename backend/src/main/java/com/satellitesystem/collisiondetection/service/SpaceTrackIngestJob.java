package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.FetchTooSoonException;
import com.satellitesystem.collisiondetection.exception.SpaceTrackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Refreshes the data from Space-Track on a timer and re-runs collision detection,
 * so a deployed server stays up to date without anyone pressing a button.
 * Only created when spacetrack.ingest.enabled=true. FetchCooldown still applies, so it can't fetch more than once an hour.
 */
@Component
@ConditionalOnProperty(name = "spacetrack.ingest.enabled", havingValue = "true")
public class SpaceTrackIngestJob {

    private static final Logger log = LoggerFactory.getLogger(SpaceTrackIngestJob.class);

    private final SpaceTrackApiService spaceTrackApiService;
    private final CollisionDetectionService collisionDetectionService;

    public SpaceTrackIngestJob(SpaceTrackApiService spaceTrackApiService,
                               CollisionDetectionService collisionDetectionService) {
        this.spaceTrackApiService = spaceTrackApiService;
        this.collisionDetectionService = collisionDetectionService;
    }

    //fixedDelay: the next run starts this long after the previous one finished, so runs can never overlap
    //the first run waits a few minutes after startup, so restarting the app repeatedly doesn't hit Space-Track each time
    @Scheduled(initialDelayString = "${spacetrack.ingest.initial-delay:PT7M}",
               fixedDelayString = "${spacetrack.ingest.interval:PT6H}")
    public void refresh() {
        try {
            int count = spaceTrackApiService.fetchAndStoreSatellites();
            int collisions = collisionDetectionService.detectCollisions().size();
            log.info("Scheduled refresh: {} satellites loaded, {} potential collisions found", count, collisions);
        } catch (SpaceTrackException | FetchTooSoonException e) {
            //the old data is kept when a fetch fails, so just log it and try again at the next run
            log.warn("Scheduled refresh skipped: {}", e.getMessage());
        }
    }
}
