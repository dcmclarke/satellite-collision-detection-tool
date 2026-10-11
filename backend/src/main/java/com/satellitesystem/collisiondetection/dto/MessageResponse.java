package com.satellitesystem.collisiondetection.dto;

//JSON body returned by action endpoints, e.g. {"message": "Collision detection complete! ..."}
public record MessageResponse(String message) {
}
