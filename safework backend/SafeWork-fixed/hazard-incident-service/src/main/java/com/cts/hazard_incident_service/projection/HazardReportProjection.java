package com.cts.hazard_incident_service.projection;


import com.cts.hazard_incident_service.enums.HazardStatus;

import java.time.LocalDate;

public interface HazardReportProjection {

    Long getHazardId();
    String getHazardDescription();
    String getHazardLocation();
    LocalDate getHazardDate();
    HazardStatus getHazardStatus();
    Long getEmployeeId();
}