package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CollisionPredictionService {

    private final CollisionPredictionRepository repository;

    public CollisionPredictionService(CollisionPredictionRepository repository) {
        this.repository = repository;
    }

    public List<CollisionPrediction> getActivePredictions() {
        return repository.findByStatus("ACTIVE");
    }

    public List<CollisionPrediction> getCriticalPredictions() {
        return repository.findByRiskLevel("CRITICAL");
    }
}
