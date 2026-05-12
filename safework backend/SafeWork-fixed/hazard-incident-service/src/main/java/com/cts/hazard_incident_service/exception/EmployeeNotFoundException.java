package com.cts.hazard_incident_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// it tells spring that this 404 error
@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id " + id);
    }
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}



