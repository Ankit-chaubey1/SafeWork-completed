package com.cts.hazard_incident_service.service;



import com.cts.hazard_incident_service.dto.HazardRequestDto;
import com.cts.hazard_incident_service.entity.Hazard;
import com.cts.hazard_incident_service.enums.HazardStatus;
import com.cts.hazard_incident_service.projection.HazardReportProjection;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IHazardService  {
    List<HazardReportProjection> getAllHazards();

    HazardRequestDto addHazard(Long employeeId, HazardRequestDto hazardRequestDto);

    HazardReportProjection getHazardById(Long hazardId);

    HazardRequestDto updateHazard(Long hazardId, Long employeeId, String role, HazardRequestDto hazardRequestDto);

    List<HazardReportProjection> getHazardsByEmployee(Long employeeId);

    List<HazardReportProjection> getHazardsByStatus(HazardStatus status);

    Map<String, Long> getHazardSummary();

    @Transactional
    String deleteHazard(Long hazardId, Long employeeId, String role);}
