package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.dto.AlertResponse;
import com.satellitesystem.collisiondetection.model.Alert;
import com.satellitesystem.collisiondetection.service.AlertService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertResponse> getAllAlerts() {
        return toResponses(service.getAllAlerts());
    }

    @GetMapping("/unacknowledged")
    public List<AlertResponse> getUnacknowledgedAlerts() {
        return toResponses(service.getUnacknowledgedAlerts());
    }

    @GetMapping("/recent")
    public List<AlertResponse> getRecentAlerts() {
        return toResponses(service.getRecentAlerts());
    }

    @PostMapping("/{id}/acknowledge")
    public AlertResponse acknowledgeAlert(@PathVariable Long id) {
        Alert alert = service.acknowledgeAlert(id);
        return alert == null ? null : AlertResponse.from(alert);
    }

    //get in memory alerts (for demo)
    @GetMapping("/in-memory")
    public List<AlertResponse> getInMemoryAlerts() {
        return toResponses(service.getInMemoryAlerts());
    }

    private List<AlertResponse> toResponses(List<Alert> alerts) {
        return alerts.stream().map(AlertResponse::from).toList();
    }
}
