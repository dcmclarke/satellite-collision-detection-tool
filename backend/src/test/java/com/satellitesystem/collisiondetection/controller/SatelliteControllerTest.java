package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.exception.ResourceNotFoundException;
import com.satellitesystem.collisiondetection.exception.SpaceTrackException;
import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
import com.satellitesystem.collisiondetection.service.SpaceTrackApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//loads only the web layer (controller + GlobalExceptionHandler) with mocked services, so no database is needed
@WebMvcTest(SatelliteController.class)
class SatelliteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SatelliteService satelliteService;

    @MockitoBean
    private SpaceTrackApiService spaceTrackApiService;

    @MockitoBean
    private CollisionDetectionService collisionDetectionService;

    @Test
    void getSatellite_UnknownId_Returns404WithMessage() throws Exception {
        when(satelliteService.getSatellite(42L)).thenThrow(new ResourceNotFoundException("Satellite not found: 42"));

        mockMvc.perform(get("/api/satellites/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Satellite not found: 42"));
    }

    @Test
    void fetchSpaceTrackData_SpaceTrackFails_Returns502WithMessage() throws Exception {
        when(spaceTrackApiService.fetchAndStoreSatellites())
                .thenThrow(new SpaceTrackException("Space-Track login failed - check the configured username and password"));

        mockMvc.perform(post("/api/satellites/fetch-spacetrack-data"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Space-Track login failed - check the configured username and password"));
    }

    @Test
    void detectCollisions_ReturnsJsonMessage() throws Exception {
        when(collisionDetectionService.detectCollisions()).thenReturn(List.of());
        when(collisionDetectionService.getSatelliteCount()).thenReturn(4L);

        mockMvc.perform(post("/api/satellites/detect-collisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Collision detection complete! Found 0 potential collisions. Total satellites analysed: 4"));
    }
}
