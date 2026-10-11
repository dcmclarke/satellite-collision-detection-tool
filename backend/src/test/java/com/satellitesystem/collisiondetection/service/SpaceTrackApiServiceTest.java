package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.SpaceTrackException;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

//runs the real HTTP code against a small fake Space-Track server on localhost, so no account or network is needed
//the repository and SatelliteService are Mockito mocks, so no database is needed either
class SpaceTrackApiServiceTest {

    private static final String SESSION_COOKIE = "chocolatechip=test-session";
    private static final String TWO_SATELLITES = """
            [
              {"OBJECT_NAME": "SAT A", "NORAD_CAT_ID": "1", "INCLINATION": "51.6", "RA_OF_ASC_NODE": "247.4", "MEAN_MOTION": "15.5"},
              {"OBJECT_NAME": "SAT B", "NORAD_CAT_ID": "2", "INCLINATION": "53.0", "RA_OF_ASC_NODE": "327.8", "MEAN_MOTION": "15.0"}
            ]
            """;

    private HttpServer fakeSpaceTrack;
    private SatelliteRepository satelliteRepository;
    private SatelliteService satelliteService;
    private SpaceTrackApiService service;

    //what the fake server should answer; each test changes these
    private String loginResponse = "\"\"";
    private int dataStatus = 200;

    @BeforeEach
    void setUp() throws IOException {
        fakeSpaceTrack = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        fakeSpaceTrack.createContext("/ajaxauth/login", exchange -> {
            exchange.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "; Path=/");
            respond(exchange, 200, loginResponse);
        });
        fakeSpaceTrack.createContext("/basicspacedata", exchange -> {
            //like the real API: no session cookie from the login, no data
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie == null || !cookie.contains(SESSION_COOKIE)) {
                respond(exchange, 401, "not logged in");
            } else {
                respond(exchange, dataStatus, dataStatus == 200 ? TWO_SATELLITES : "server error");
            }
        });
        fakeSpaceTrack.start();

        satelliteRepository = mock(SatelliteRepository.class);
        satelliteService = mock(SatelliteService.class);
        String baseUrl = "http://localhost:" + fakeSpaceTrack.getAddress().getPort();
        //zero interval: the cooldown has its own tests, here every fetch should go through
        FetchCooldown noCooldown = new FetchCooldown(Duration.ZERO);
        service = new SpaceTrackApiService(satelliteRepository, satelliteService, noCooldown,
                "test-user", "test-password", baseUrl);
    }

    @AfterEach
    void tearDown() {
        fakeSpaceTrack.stop(0);
    }

    @Test
    void fetch_Success_ReplacesDataWithDownloadedSatellites() {
        int count = service.fetchAndStoreSatellites();

        assertEquals(2, count);
        verify(satelliteService).deleteAllData();
        verify(satelliteRepository).saveAll(anyList());
    }

    @Test
    void fetch_LoginFails_ThrowsAndKeepsExistingData() {
        loginResponse = "{\"Login\":\"Failed\"}";

        SpaceTrackException e = assertThrows(SpaceTrackException.class, () -> service.fetchAndStoreSatellites());

        assertTrue(e.getMessage().contains("login failed"));
        verify(satelliteService, never()).deleteAllData();
        verify(satelliteRepository, never()).saveAll(anyList());
    }

    @Test
    void fetch_DataRequestFails_ThrowsAndKeepsExistingData() {
        dataStatus = 500;

        SpaceTrackException e = assertThrows(SpaceTrackException.class, () -> service.fetchAndStoreSatellites());

        assertTrue(e.getMessage().contains("500"));
        verify(satelliteService, never()).deleteAllData();
        verify(satelliteRepository, never()).saveAll(anyList());
    }

    @Test
    void fetch_ServerUnreachable_ThrowsAndKeepsExistingData() {
        fakeSpaceTrack.stop(0);

        assertThrows(SpaceTrackException.class, () -> service.fetchAndStoreSatellites());

        verify(satelliteService, never()).deleteAllData();
        verify(satelliteRepository, never()).saveAll(anyList());
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
