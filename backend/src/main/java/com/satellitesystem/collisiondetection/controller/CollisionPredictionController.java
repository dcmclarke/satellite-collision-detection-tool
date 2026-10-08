package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.service.CollisionPredictionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/collisions")
public class CollisionPredictionController {

    private final CollisionPredictionService service;

    public CollisionPredictionController(CollisionPredictionService service) {
        this.service = service;
    }

    @GetMapping("/active")
    public List<CollisionPrediction> getActiveCollisions() {
        return service.getActivePredictions();
    }

    @GetMapping("/critical")
    public List<CollisionPrediction> getCriticalCollisions() {
        return service.getCriticalPredictions();
    }
}