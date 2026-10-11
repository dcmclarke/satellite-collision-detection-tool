package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.FetchTooSoonException;
import com.satellitesystem.collisiondetection.exception.SpaceTrackException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpaceTrackIngestJobTest {

    private final SpaceTrackApiService spaceTrackApiService = mock(SpaceTrackApiService.class);
    private final CollisionDetectionService collisionDetectionService = mock(CollisionDetectionService.class);
    private final SpaceTrackIngestJob job = new SpaceTrackIngestJob(spaceTrackApiService, collisionDetectionService);

    @Test
    void refresh_FetchesThenRunsDetection() {
        when(spaceTrackApiService.fetchAndStoreSatellites()).thenReturn(500);
        when(collisionDetectionService.detectCollisions()).thenReturn(List.of());

        job.refresh();

        InOrder order = inOrder(spaceTrackApiService, collisionDetectionService);
        order.verify(spaceTrackApiService).fetchAndStoreSatellites();
        order.verify(collisionDetectionService).detectCollisions();
    }

    @Test
    void refresh_SpaceTrackFails_SkipsDetectionWithoutThrowing() {
        when(spaceTrackApiService.fetchAndStoreSatellites()).thenThrow(new SpaceTrackException("Space-Track is down"));

        assertDoesNotThrow(job::refresh);

        verify(collisionDetectionService, never()).detectCollisions();
    }

    @Test
    void refresh_TooSoon_SkipsDetectionWithoutThrowing() {
        when(spaceTrackApiService.fetchAndStoreSatellites()).thenThrow(new FetchTooSoonException(Duration.ofMinutes(5)));

        assertDoesNotThrow(job::refresh);

        verify(collisionDetectionService, never()).detectCollisions();
    }
}
