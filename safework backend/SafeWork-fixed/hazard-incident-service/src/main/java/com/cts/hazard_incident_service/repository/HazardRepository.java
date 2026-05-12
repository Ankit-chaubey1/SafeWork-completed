package com.cts.hazard_incident_service.repository;


import com.cts.hazard_incident_service.entity.Hazard;
import com.cts.hazard_incident_service.enums.HazardStatus;
import com.cts.hazard_incident_service.projection.HazardReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HazardRepository extends JpaRepository<Hazard, Long> {

    @Query("""
        SELECT h.hazardId AS hazardId,
               h.hazardDescription AS hazardDescription,
               h.hazardLocation AS hazardLocation,
               h.hazardDate AS hazardDate,
               h.hazardStatus AS hazardStatus,
               h.employeeId AS employeeId
        FROM Hazard h
    """)
    List<HazardReportProjection> getAllHazards();

    Optional<HazardReportProjection> findByHazardId(Long hazardId);

    List<HazardReportProjection> findByEmployeeId(Long employeeId);

    List<HazardReportProjection> findByHazardStatus(HazardStatus status);


}

