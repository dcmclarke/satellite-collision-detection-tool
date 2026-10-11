package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.dto.CollisionPredictionResponse;
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
    public List<CollisionPredictionResponse> getActiveCollisions() {
        return service.getActivePredictions().stream().map(CollisionPredictionResponse::from).toList();
    }

    @GetMapping("/critical")
    public List<CollisionPredictionResponse> getCriticalCollisions() {
        return service.getCriticalPredictions().stream().map(CollisionPredictionResponse::from).toList();
    }
}