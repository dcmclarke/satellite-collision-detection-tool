package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SatelliteService {

    private final SatelliteRepository repository;
    private final AlertRepository alertRepository;
    private final CollisionPredictionRepository collisionPredictionRepository;

    //dependency injection: Spring passes the repositories in when it creates this service
    public SatelliteService(SatelliteRepository repository,
                            AlertRepository alertRepository,
                            CollisionPredictionRepository collisionPredictionRepository) {
        this.repository = repository;
        this.alertRepository = alertRepository;
        this.collisionPredictionRepository = collisionPredictionRepository;
    }

    public List<Satellite> getAllSatellites() {
        return repository.findAll();
    }

    public Satellite getSatellite(Long id) {
        return repository.findById(id).orElse(null);
    }

    //clears all data before a new dataset is loaded, to stop satellites stacking up
    //order matters: alerts reference predictions, and predictions reference satellites
    //one transaction, so either all three tables are cleared or none are
    @Transactional
    public void deleteAllData() {
        alertRepository.deleteAll();
        collisionPredictionRepository.deleteAll();
        repository.deleteAll();
    }
}
