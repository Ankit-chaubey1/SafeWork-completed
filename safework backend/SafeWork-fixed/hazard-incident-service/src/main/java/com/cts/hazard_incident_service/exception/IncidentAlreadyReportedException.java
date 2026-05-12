package com.cts.hazard_incident_service.exception;

public class IncidentAlreadyReportedException extends RuntimeException {
    public IncidentAlreadyReportedException() {
        super("Cannot create incident: hazard is not PENDING.");
    }

    public IncidentAlreadyReportedException(String message){
        super(message);
    }
}
