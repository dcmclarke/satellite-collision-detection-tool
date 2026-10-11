package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.ResourceNotFoundException;
import com.satellitesystem.collisiondetection.model.Alert;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    public List<Alert> getAllAlerts() {
        return repository.findAll();
    }

    public List<Alert> getUnacknowledgedAlerts() {
        return repository.findByAcknowledged(false);
    }

    public List<Alert> getRecentAlerts() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return repository.findBySentAtAfter(oneDayAgo);
    }

    public Alert saveAlert(Alert alert) {
        return repository.save(alert);
    }

    public Alert acknowledgeAlert(Long id) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.setAcknowledged(true);
        return repository.save(alert);
    }
}
