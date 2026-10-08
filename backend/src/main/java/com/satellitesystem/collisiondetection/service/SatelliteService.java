package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SatelliteService {

    private final SatelliteRepository repository;

    //dependency injection: Spring passes the repository in when it creates this service
    public SatelliteService(SatelliteRepository repository) {
        this.repository = repository;
    }

    public List<Satellite> getAllSatellites() {
        return repository.findAll();
    }

    public Satellite getSatellite(Long id) {
        return repository.findById(id).orElse(null);
    }
}
