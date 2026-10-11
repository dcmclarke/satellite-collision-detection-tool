package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.AlertRepository;
import com.satellitesystem.collisiondetection.repository.CollisionPredictionRepository;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

//proves detectCollisions() is all-or-nothing: if it fails part way, the previous results are still there
@SpringBootTest
class CollisionDetectionRollbackTest {

    @Autowired
    private CollisionDetectionService collisionService;

    @Autowired
    private SatelliteRepository satelliteRepository;

    @Autowired
    private CollisionPredictionRepository collisionRepository;

    @Autowired
    private AlertRepository alertRepository;

    //a spy is the real AlertService, but the test can make individual methods throw
    @MockitoSpyBean
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        collisionRepository.deleteAll();
        satelliteRepository.deleteAll();
    }

    @Test
    void detectCollisions_FailsPartWay_KeepsPreviousResults() {
        //a first, successful run: two close satellites give one prediction and one alert
        satelliteRepository.save(new Satellite("SAT1", "1", 0.0, 0.0, 400.0));
        satelliteRepository.save(new Satellite("SAT2", "2", 0.01, 0.01, 400.5));
        collisionService.detectCollisions();
        assertEquals(1, collisionRepository.count());
        assertEquals(1, alertRepository.count());

        //second run: the old results get deleted, then saving the first new alert fails
        doThrow(new RuntimeException("simulated database failure")).when(alertService).saveAlert(any());
        try {
            assertThrows(RuntimeException.class, () -> collisionService.detectCollisions());
        } finally {
            doCallRealMethod().when(alertService).saveAlert(any());
        }

        //without @Transactional these would be 1 new prediction and 0 alerts; with it, the deletes were rolled back
        assertEquals(1, collisionRepository.count(), "old prediction should survive the failed run");
        assertEquals(1, alertRepository.count(), "old alert should survive the failed run");
    }
}
