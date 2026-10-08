package com.satellitesystem.collisiondetection.dto;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.satellitesystem.collisiondetection.model.Alert;
import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.model.Satellite;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

//the DTOs replaced entities in the API, so their JSON must match what the entities produced (the frontend relies on it)
class ResponseJsonShapeTest {

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void satelliteResponse_HasSameJsonAsEntity() {
        Satellite satellite = satellite(1L, "SAT1");

        assertEquals(mapper.valueToTree(satellite), mapper.valueToTree(SatelliteResponse.from(satellite)));
    }

    @Test
    void alertResponse_HasSameJsonAsEntity() {
        CollisionPrediction prediction = new CollisionPrediction();
        prediction.setId(10L);
        prediction.setSatellite1(satellite(1L, "SAT1"));
        prediction.setSatellite2(satellite(2L, "SAT2"));
        prediction.setPredictedTime(LocalDateTime.of(2026, 1, 1, 12, 0));
        prediction.setMinimumDistance(1.5);
        prediction.setProbabilityScore(90);
        prediction.setRiskLevel("CRITICAL");
        prediction.setStatus("ACTIVE");

        Alert alert = new Alert();
        alert.setId(100L);
        alert.setPrediction(prediction);
        alert.setAlertLevel("CRITICAL");
        alert.setMessage("COLLISION ALERT");
        alert.setSentAt(LocalDateTime.of(2026, 1, 1, 12, 0));

        //alert JSON nests the prediction, which nests two satellites, so this checks all three DTOs
        assertEquals(mapper.valueToTree(alert), mapper.valueToTree(AlertResponse.from(alert)));
    }

    private Satellite satellite(Long id, String name) {
        Satellite satellite = new Satellite(name, "2554" + id, 51.6, 247.4, 1550.1);
        satellite.setId(id);
        return satellite;
    }
}
