package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.dto.MessageResponse;
import com.satellitesystem.collisiondetection.dto.SatelliteResponse;
import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.SpaceTrackApiService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/satellites")
public class SatelliteController {

    private final SatelliteService service;
    private final SpaceTrackApiService spaceTrackApiService;
    private final CollisionDetectionService collisionDetectionService;

    public SatelliteController(SatelliteService service,
                               SpaceTrackApiService spaceTrackApiService,
                               CollisionDetectionService collisionDetectionService) {
        this.service = service;
        this.spaceTrackApiService = spaceTrackApiService;
        this.collisionDetectionService = collisionDetectionService;
    }

    //trigger collision detection for all satellites
    //POST http://localhost:8080/api/satellites/detect-collisions
    @PostMapping("/detect-collisions")
    public ResponseEntity<MessageResponse> detectCollisions() {
        List<CollisionPrediction> predictions = collisionDetectionService.detectCollisions();
        return ResponseEntity.ok(new MessageResponse("Collision detection complete! Found " + predictions.size()
                + " potential collisions. Total satellites analysed: " + collisionDetectionService.getSatelliteCount()));
    }

    @GetMapping
    public List<SatelliteResponse> getAllSatellites() {
        return service.getAllSatellites().stream().map(SatelliteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SatelliteResponse getSatellite(@PathVariable Long id) {
        return SatelliteResponse.from(service.getSatellite(id));
    }

    /*PRIMARY METHOD: fetches live data from Space-Track api
    *POST http://localhost:8080/api/satellites/fetch-spacetrack-data
    */
    @PostMapping("/fetch-spacetrack-data")
    public ResponseEntity<MessageResponse> fetchSpaceTrackData() {
        //clear all data first to fix the satellite stacking issue
        service.deleteAllData();

        //throws SpaceTrackException on failure, which GlobalExceptionHandler turns into a 502
        int count = spaceTrackApiService.fetchAndStoreSatellites();
        long totalCount = spaceTrackApiService.getSatelliteCount();
        return ResponseEntity.ok(new MessageResponse("Successfully fetched " + count
                + " satellites from Space-Track! Total satellites in database: " + totalCount));
    }

    /**
     * BACKUP:loads embedded satellite data for demo if needed
     * Use when Space-Track API is unavailable or offline dev
     * POST http://localhost:8080/api/satellites/load-backup-data
     */

    @PostMapping("/load-backup-data")
    public ResponseEntity<MessageResponse> loadBackupData() {
        //clear all data first to fix the satellite stacking issue
        service.deleteAllData();

        int count = spaceTrackApiService.loadBackupData();
        long totalCount = spaceTrackApiService.getSatelliteCount();
        return ResponseEntity.ok(new MessageResponse("Loaded " + count
                + " satellites from backup dataset (demo mode). Total satellites in database: " + totalCount));
    }
}
