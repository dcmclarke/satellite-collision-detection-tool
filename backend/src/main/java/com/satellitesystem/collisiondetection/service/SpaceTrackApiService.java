package com.satellitesystem.collisiondetection.service;

import com.satellitesystem.collisiondetection.exception.SpaceTrackException;
import com.satellitesystem.collisiondetection.model.Satellite;
import com.satellitesystem.collisiondetection.repository.SatelliteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpaceTrackApiService {

    private static final Logger log = LoggerFactory.getLogger(SpaceTrackApiService.class);

    private final SatelliteRepository satelliteRepository;
    private final String username;
    private final String password;
    private final String apiUrl;

    public SpaceTrackApiService(SatelliteRepository satelliteRepository,
                                @Value("${spacetrack.api.username}") String username,
                                @Value("${spacetrack.api.password}") String password,
                                @Value("${spacetrack.api.url}") String apiUrl) {
        this.satelliteRepository = satelliteRepository;
        this.username = username;
        this.password = password;
        this.apiUrl = apiUrl;
    }

    //fetches sat data from Space-Track.org api, up to 500 sats (see limit in dataUrl)
    //returns how many were saved, or throws SpaceTrackException if Space-Track can't be used
    public int fetchAndStoreSatellites() {
        log.info("Starting Space-Track API fetch");

        try {
            //create cookie manager
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);

            //create HTTP client
            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cookieManager)
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            //step 1: login using /ajaxauth/login (like the Python client does)
            String loginUrl = "https://www.space-track.org/ajaxauth/login";
            log.info("Logging in to Space-Track");

            String loginBody = "identity=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(loginUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            log.info("Space-Track login response status: {}", loginResponse.statusCode());

            //check if login succeeded
            if (loginResponse.body().contains("\"Login\":\"Failed\"")) {
                log.warn("Space-Track login failed - check the configured username and password");
                throw new SpaceTrackException("Space-Track login failed - check the configured username and password");
            }

            log.info("Space-Track login successful");

            //step 2: fetch satellite data
            //get 500 active satellites (updated in last 30 days)
            String dataUrl = apiUrl + "/basicspacedata/query/class/gp/decay_date/null-val/epoch/%3Enow-30/orderby/norad_cat_id/limit/500/format/json";
            log.info("Fetching satellite data");

            HttpRequest dataRequest = HttpRequest.newBuilder()
                    .uri(URI.create(dataUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> dataResponse = client.send(dataRequest, HttpResponse.BodyHandlers.ofString());

            log.info("Space-Track data response status: {}", dataResponse.statusCode());

            if (dataResponse.statusCode() != 200) {
                throw new SpaceTrackException("Space-Track data request failed with status " + dataResponse.statusCode());
            }

            log.info("Data received, parsing");

            int count = parseSatelliteData(dataResponse.body());

            log.info("Successfully fetched {} satellites from Space-Track", count);
            return count;

        } catch (IOException e) {
            log.error("Error fetching Space-Track data", e);
            throw new SpaceTrackException("Could not reach Space-Track: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            //restore the interrupt flag so the calling thread can still see it was interrupted
            Thread.currentThread().interrupt();
            log.error("Space-Track fetch was interrupted", e);
            throw new SpaceTrackException("Space-Track fetch was interrupted", e);
        }
    }

    //parses JSON from Space-Track & converts to sat objects
    private int parseSatelliteData(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);

            List<Satellite> satellites = new ArrayList<>();

            //loop through each sat in json array
            for (JsonNode node : rootNode) {
                Satellite satellite = new Satellite();

                //extract data from json
                satellite.setName(node.get("OBJECT_NAME").asText());
                satellite.setNoradId(node.get("NORAD_CAT_ID").asText());

                //orbital elements
                satellite.setLatitude(node.get("INCLINATION").asDouble());
                satellite.setLongitude(node.get("RA_OF_ASC_NODE").asDouble());
                satellite.setAltitude(node.get("MEAN_MOTION").asDouble() * 100);

                satellites.add(satellite);

                //print first sat as example
                if (satellites.size() == 1) {
                    log.debug("Example satellite: {}", satellite.getName());
                }
            }

            //save all to db at once
            satelliteRepository.saveAll(satellites);
            log.info("Saved {} satellites to database", satellites.size());

            return satellites.size();
        } catch (Exception e) {
            log.error("Error parsing satellite data", e);
            return 0;
        }
    }

    //get count of sats in db
    public long getSatelliteCount() {
        return satelliteRepository.count();
    }

    /**
     * Fallback data source for demo
     * Has sats in similar LEO orbits to show collision detection works
     * Used when Space Track API is unavailable (network outage, rate limits, downtime etc.)
     *
     * TLE data snapshot: October 2024, sourced from Space-Track.org
     */
    //returns how many satellites were saved
    public int loadBackupData() {
        log.info("Loading backup satellite data for demonstration");

        //leo satellites with known proximity for collision detection demo
        String backupData = """
        [
          {"OBJECT_NAME": "ISS (ZARYA)", "NORAD_CAT_ID": "25544", 
           "INCLINATION": "51.6416", "RA_OF_ASC_NODE": "247.4627", "MEAN_MOTION": "15.50103472"},
          {"OBJECT_NAME": "STARLINK-1007", "NORAD_CAT_ID": "44713",
           "INCLINATION": "53.0532", "RA_OF_ASC_NODE": "327.8503", "MEAN_MOTION": "15.06415123"},
          {"OBJECT_NAME": "STARLINK-1020", "NORAD_CAT_ID": "44726",
           "INCLINATION": "53.0510", "RA_OF_ASC_NODE": "327.8600", "MEAN_MOTION": "15.06420000"},
          {"OBJECT_NAME": "STARLINK-1033", "NORAD_CAT_ID": "44739",
           "INCLINATION": "53.0520", "RA_OF_ASC_NODE": "327.8700", "MEAN_MOTION": "15.06418000"}
        ]
        """;

        return parseSatelliteData(backupData);
    }
}
