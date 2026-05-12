package com.cts.hazard_incident_service.projection;

import java.time.LocalDate;
import java.util.Date;

public interface IncidentReportProjection {

    Long getIncidentId();
    String getAction();
    LocalDate getIncidentDate();
    Long getOfficerId();
    Long getHazardId();
    String getHazardDescription();
}