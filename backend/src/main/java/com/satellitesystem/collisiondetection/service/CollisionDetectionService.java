package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.model.CollisionPrediction;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.model.Alert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.satellitesystem.collisiondetection.repository.AlertRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//service for detecting potential satellite collisions using distance based screening, conjunction detectio methods based on the two studies (Burgism Lechtenberg)

@Service
public class CollisionDetectionService {

    private static final Logger log = LoggerFactory.getLogger(CollisionDetectionService.class);

   //distance thresholds (km)
    private static final double COLLISION_THRESHOLD = 5.0;
    private static final double CRITICAL_DISTANCE = 2.0; // <2km = critical
    private static final double WARNING_DISTANCE = 3.5; // <3.5km = warning

    //probability score constants
    private static final int PROBABILITY_CRITICAL = 90;
    private static final int PROBABILITY_WARNING = 60;
    private static final int PROBABILITY_INFO = 30;

    private final AlertService alertService;
    private final SatelliteRepository satelliteRepository;
    private final CollisionPredictionRepository collisionRepository;
    private final AlertRepository alertRepository;

    public CollisionDetectionService(AlertService alertService,
                                     SatelliteRepository satelliteRepository,
                                     CollisionPredictionRepository collisionRepository,
                                     AlertRepository alertRepository) {
        this.alertService = alertService;
        this.satelliteRepository = satelliteRepository;
        this.collisionRepository = collisionRepository;
        this.alertRepository = alertRepository;
    }

    //main method detecitn all potential collisions in satellite population
    //checks every pair of satellites for proximity within collision threshold
    //runs in one transaction so a failure part way through rolls back the deletes too
    @Transactional
    public List<CollisionPrediction> detectCollisions() {
        log.info("Starting collision detection");

        //delete alerts first (they ref collision predictions)
        log.debug("Clearing old alerts");
        alertRepository.deleteAll();

        //delete collision predictions
        log.debug("Clearing old predictions to avoid duplicates");
        collisionRepository.deleteAll();

        //get all satellites from db
        List<Satellite> satellites = satelliteRepository.findAll();
        List<CollisionPrediction> predictions = new ArrayList<>();

        log.info("Analysing {} satellites", satellites.size());

        //check every unique pair of satellites
        //nested loop checks n*(n-1)/2 pairs, e.g. 500 satellites = 124,750 pairs
        int pairsChecked = 0;
        for (int i = 0; i < satellites.size(); i++) {
            for (int j = i + 1; j <satellites.size(); j++) {
                pairsChecked++;

                Satellite sat1 = satellites.get(i);
                Satellite sat2 = satellites.get(j);

                //calculate 3d distance between two satellites
                double distance = calculateDistance(sat1, sat2);

                //if satellites within collision threshold create prediction
                if (distance < COLLISION_THRESHOLD) {
                    CollisionPrediction prediction = createPrediction(sat1, sat2, distance);
                    predictions.add(prediction);

                    log.info("COLLISION RISK: {} and {} are {} km apart", sat1.getName(), sat2.getName(), String.format("%.2f", distance));
                }
            }
        }

        log.info("Checked {} satellite pairs", pairsChecked);
        log.info("Found {} potential collisions", predictions.size());

        //each prediction is already saved in createPrediction(), so no saveAll() is needed here
        return predictions;
    }

    private double calculateDistance(Satellite sat1, Satellite sat2) {
        double[] pos1 = CollisionMath.latLonAltToXYZ(
                sat1.getLatitude(), sat1.getLongitude(), sat1.getAltitude());
        double[] pos2 = CollisionMath.latLonAltToXYZ(
                sat2.getLatitude(), sat2.getLongitude(), sat2.getAltitude());
        return CollisionMath.euclideanDistance(pos1, pos2);
    }

    //creating CollisionPrediction object with risk assessment
    //risk levels based on distance:
    //CRITICAL (<2km): High prob of collision
    //WARNING(<2-3.5km): Moderate risk, needs monitoring
    //INFO (3.5-5km): Low risk, info alert

    private CollisionPrediction createPrediction(Satellite sat1, Satellite sat2, double distance) {
        CollisionPrediction prediction = new CollisionPrediction();

        //set satellite references
        prediction.setSatellite1(sat1);
        prediction.setSatellite2(sat2);

        //set distance & time
        prediction.setMinimumDistance(distance);
        prediction.setPredictedTime(LocalDateTime.now());

        //assign risk level & probability
        if (distance < CRITICAL_DISTANCE) {
            prediction.setRiskLevel("CRITICAL");
            prediction.setProbabilityScore(PROBABILITY_CRITICAL);
        } else if (distance < WARNING_DISTANCE) {
            prediction.setRiskLevel("WARNING");
            prediction.setProbabilityScore(PROBABILITY_WARNING);
        } else {
            prediction.setRiskLevel("INFO");
            prediction.setProbabilityScore(PROBABILITY_INFO);
        }

        prediction.setStatus("ACTIVE");
        //saved here (not at the end) because the alert below must reference a saved prediction
        prediction = collisionRepository.save(prediction);

        Alert alert = createAlert(prediction);
        alertService.saveAlert(alert);

        return prediction;
    }

    //helper to create alert from prediction
    private Alert createAlert(CollisionPrediction prediction) {
        Alert alert = new Alert();
        alert.setPrediction(prediction);
        alert.setAlertLevel(prediction.getRiskLevel());
        alert.setMessage(buildAlertMessage(prediction));
        alert.setSentAt(LocalDateTime.now());
        alert.setAcknowledged(false);
        return alert;
    }

    private String buildAlertMessage(CollisionPrediction prediction) {
        return String.format("COLLISION ALERT: %s and %s are %.2f km apart (Risk: %s)",
                prediction.getSatellite1().getName(),
                prediction.getSatellite2().getName(),
                prediction.getMinimumDistance(),
                prediction.getRiskLevel()
        );
    }

    //get count of satellites currently in db
    public long getSatelliteCount() {
        return satelliteRepository.count();
    }

    //get count of active collision predictions
    public long getCollisionCount() {
        return collisionRepository.findByStatus("ACTIVE").size();
    }
}
