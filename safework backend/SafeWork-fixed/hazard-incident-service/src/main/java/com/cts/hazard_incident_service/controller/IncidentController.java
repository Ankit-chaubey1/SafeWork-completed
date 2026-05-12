package com.cts.hazard_incident_service.controller;

import com.cts.hazard_incident_service.dto.IncidentRequestDto;
import com.cts.hazard_incident_service.projection.IncidentReportProjection;
import com.cts.hazard_incident_service.service.IIncidentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidents")
@Slf4j
public class IncidentController {

    private final IIncidentService incidentService;

    public IncidentController(IIncidentService incidentService) {
        this.incidentService = incidentService;
    }


    @PostMapping("/{hazardId}")
    public ResponseEntity<IncidentRequestDto> addIncident(
            @PathVariable Long hazardId,
            @RequestHeader("X-User-Id") Long officerId,
            @Valid @RequestBody IncidentRequestDto request) {

        log.info("Creating incident for hazardId: {} by officerId: {}", hazardId, officerId);

        IncidentRequestDto savedIncident =
                incidentService.addIncident(hazardId, officerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedIncident);
    }

    /** View all incidents */
    @GetMapping("/getAllIncidents")
    public ResponseEntity<List<IncidentReportProjection>> getIncidents() {
        return ResponseEntity.ok(incidentService.getIncidents());
    }

    /** View incident by incidentId */
    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentReportProjection> getIncidentById(
            @PathVariable Long incidentId) {

        return ResponseEntity.ok(incidentService.getIncidentById(incidentId));
    }

    /** View incident by hazardId */
    @GetMapping("/hazard/{hazardId}")
    public ResponseEntity<IncidentReportProjection> getIncidentByHazardId(
            @PathVariable Long hazardId) {

        return ResponseEntity.ok(incidentService.getIncidentByHazardId(hazardId));
    }

    /** View incidents handled by officer */
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<IncidentReportProjection>> getIncidentsByOfficer(
            @PathVariable Long officerId) {

        return ResponseEntity.ok(incidentService.getIncidentsByOfficer(officerId));
    }

    /** Update an incident */
    @PutMapping("/{incidentId}")
    public ResponseEntity<IncidentRequestDto> updateIncident(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentRequestDto request) {

        log.info("Updating incident: {}", incidentId);
        IncidentRequestDto updatedIncident = incidentService.updateIncident(incidentId, request);
        return ResponseEntity.ok(updatedIncident);
    }

    /** Delete an incident */
    @DeleteMapping("/{incidentId}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long incidentId) {
        log.info("Deleting incident: {}", incidentId);
        incidentService.deleteIncident(incidentId);
        return ResponseEntity.noContent().build();
    }
}