package com.cts.hazard_incident_service.repository;

import com.cts.hazard_incident_service.entity.Incident;
import com.cts.hazard_incident_service.projection.IncidentReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByHazard_HazardId(Long hazardId);

    @Query("""
        SELECT i.incidentId AS incidentId,
               i.action AS action,
               i.incidentDate AS incidentDate,
               i.officerId AS officerId,
               h.hazardId AS hazardId,
               h.hazardDescription AS hazardDescription
        FROM Incident i
        JOIN i.hazard h
    """)
    List<IncidentReportProjection> getIncidents();

    @Query("""
        SELECT i.incidentId AS incidentId,
               i.action AS action,
               i.incidentDate AS incidentDate,
               i.officerId AS officerId,
               h.hazardId AS hazardId,
               h.hazardDescription AS hazardDescription
        FROM Incident i
        JOIN i.hazard h
        WHERE i.officerId = :officerId
    """)
    List<IncidentReportProjection> findByOfficerId(Long officerId);
}
