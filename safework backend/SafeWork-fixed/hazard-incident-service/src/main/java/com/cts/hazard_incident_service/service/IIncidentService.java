package com.cts.hazard_incident_service.service;

import com.cts.hazard_incident_service.dto.IncidentRequestDto;
import com.cts.hazard_incident_service.entity.Incident;
import com.cts.hazard_incident_service.projection.IncidentReportProjection;

import java.util.List;

public interface IIncidentService {

    List<IncidentReportProjection> getIncidents();

    IncidentRequestDto addIncident(Long hazardId, Long officerId, IncidentRequestDto incidentRequestDto);

    IncidentReportProjection getIncidentById(Long incidentId);

    IncidentReportProjection getIncidentByHazardId(Long hazardId);

    List<IncidentReportProjection> getIncidentsByOfficer(Long officerId);

    IncidentRequestDto updateIncident(Long incidentId, IncidentRequestDto request);

    void deleteIncident(Long incidentId);
}
