package com.satellitesystem.collisiondetection.controller;

import com.satellitesystem.collisiondetection.service.CollisionDetectionService;
import com.satellitesystem.collisiondetection.service.SatelliteService;
import com.satellitesystem.collisiondetection.service.SpaceTrackApiService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//the default (production) setting: the endpoints that replace data must refuse without touching any service
@WebMvcTest(controllers = SatelliteController.class, properties = "app.manual-data-refresh.enabled=false")
class SatelliteControllerRefreshDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SatelliteService satelliteService;

    @MockitoBean
    private SpaceTrackApiService spaceTrackApiService;

    @MockitoBean
    private CollisionDetectionService collisionDetectionService;

    @ParameterizedTest
    @ValueSource(strings = {"/fetch-spacetrack-data", "/load-backup-data", "/detect-collisions"})
    void dataChangingEndpoint_WhenDisabled_Returns403AndDoesNothing(String endpoint) throws Exception {
        mockMvc.perform(post("/api/satellites" + endpoint))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(spaceTrackApiService, collisionDetectionService, satelliteService);
    }
}
