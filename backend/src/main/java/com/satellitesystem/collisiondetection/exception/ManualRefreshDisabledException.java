package com.satellitesystem.collisiondetection.exception;

//thrown when a data-changing endpoint is called but app.manual-data-refresh.enabled is false, turned into a 403
public class ManualRefreshDisabledException extends RuntimeException {

    public ManualRefreshDisabledException() {
        super("Manual data refresh is turned off on this server. "
                + "It's enabled when running locally with the 'dev' Spring profile.");
    }
}
