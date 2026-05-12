package com.cts.hazard_incident_service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found with id "+id);
    }
}
