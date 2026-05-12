package com.cts.hazard_incident_service.exception;

public class HazardNotFoundException extends RuntimeException {
    public HazardNotFoundException(Long id) {
        super("Hazard not found with id "+ id);
    }
}
