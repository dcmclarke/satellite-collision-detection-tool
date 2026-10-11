package com.satellitesystem.collisiondetection.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

//turns on Spring's @Scheduled support, but only when the scheduled Space-Track refresh is enabled
//keeping it conditional means tests and local runs never start a background job by accident
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "spacetrack.ingest.enabled", havingValue = "true")
public class SchedulingConfig {
}
