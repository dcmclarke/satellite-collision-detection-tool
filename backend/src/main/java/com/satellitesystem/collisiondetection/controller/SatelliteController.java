package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.SpaceTrackApiService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
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
    //POST http://localhost:8080/api/satellites/detection-collisions
    @PostMapping("/detect-collisions")
    public String detectCollisions() {
        List<CollisionPrediction> predictions = collisionDetectionService.detectCollisions();
        return "Collision detection complete! Found " + predictions.size() + " potential collisions. "
                + "Total satellites analysed: " + collisionDetectionService.getSatelliteCount();
    }

    @GetMapping
    public List<Satellite> getAllSatellites() {
        return service.getAllSatellites();
    }

    @GetMapping("/{id}")
    public Satellite getSatellite(@PathVariable Long id) {
        return service.getSatellite(id);
    }

    /*PRIMARY METHOD: fetches live data from Space-Track api
    *POST http://localhost:8080/api/satellites/fetch-spacetrack-data
    */
    @PostMapping("/fetch-spacetrack-data")
    public String fetchSpaceTrackData() {
        //clear all data first to fix the satellite stacking issue
        service.deleteAllData();

        String result = spaceTrackApiService.fetchAndStoreSatellites();
        long totalCount = spaceTrackApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }

    /**
     * BACKUP:loads embedded satellite data for demo if needed
     * Use when Space-Track API is unavailable or offline dev
     * POST http://localhost:8080/api/satellites/load-backup-data
     */

    @PostMapping("/load-backup-data")
    public String loadBackupData() {
        //clear all data first to fix the satellite stacking issue
        service.deleteAllData();

        String result = spaceTrackApiService.loadBackupData();
        long totalCount = spaceTrackApiService.getSatelliteCount();
        return result + " Total satellites in database: " + totalCount;
    }
}
