package com.satellitesystem.collisiondetection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns("http://localhost:3000", "https://*.netlify.app")
                    //only the methods the API actually has; OPTIONS is the browser's CORS preflight request
                    .allowedMethods("GET", "POST", "OPTIONS");
            }
        };
    }
}
