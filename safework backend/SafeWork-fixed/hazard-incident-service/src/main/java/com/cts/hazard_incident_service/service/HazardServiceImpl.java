package com.cts.hazard_incident_service.service;

import com.cts.hazard_incident_service.dto.HazardRequestDto;
import com.cts.hazard_incident_service.entity.Hazard;
import com.cts.hazard_incident_service.enums.HazardStatus;
import com.cts.hazard_incident_service.exception.EmployeeNotFoundException;
import com.cts.hazard_incident_service.exception.HazardNotFoundException;
import com.cts.hazard_incident_service.exception.IncidentAlreadyReportedException;
import com.cts.hazard_incident_service.exception.InvalidEmployeeException;
import com.cts.hazard_incident_service.exception.ServiceUnavailableException;
import com.cts.hazard_incident_service.feignClient.EmployeeClient;
import com.cts.hazard_incident_service.feignClient.NotificationClient;
import com.cts.hazard_incident_service.feignClient.UserClient;
import com.cts.hazard_incident_service.enums.NotificationCategory;
import com.cts.hazard_incident_service.projection.HazardReportProjection;
import com.cts.hazard_incident_service.repository.HazardRepository;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HazardServiceImpl implements IHazardService {

    private final HazardRepository hazardRepository;
    private final EmployeeClient employeeClient;
    private final NotificationClient notificationClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    public HazardServiceImpl(
            HazardRepository hazardRepository,
            EmployeeClient employeeClient,
            NotificationClient notificationClient) {
        this.hazardRepository = hazardRepository;
        this.employeeClient = employeeClient;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<HazardReportProjection> getAllHazards() {
        log.info("Fetching all hazards");
        List<HazardReportProjection> hazards = hazardRepository.getAllHazards();
        log.info("Total hazards fetched: {}", hazards.size());
        return hazards;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public HazardReportProjection getHazardById(Long hazardId) {

        log.info("Fetching hazard with id: {}", hazardId);

        HazardReportProjection hazard = hazardRepository.findByHazardId(hazardId)
                .orElseThrow(() -> {
                    log.warn("Hazard not found with id: {}", hazardId);
                    return new HazardNotFoundException(hazardId);
                });

        log.info("Hazard found with id: {}", hazardId);
        return hazard;
    }

    @Override
    @Transactional
    public HazardRequestDto addHazard(Long employeeId, HazardRequestDto dto) {

        log.info("Attempting to create hazard for employeeId: {}", employeeId);

        try {
            // employeeClient.getEmployeeById(employeeId);
            userClient.getUserById(employeeId);
            log.info("Employee validation successful for employeeId: {}", employeeId);
        } catch (FeignException.NotFound e) {
            log.error("Employee not found with id: {}", employeeId);
            throw new EmployeeNotFoundException("No employee found with id: " + employeeId);
        } catch (FeignException e) {
            log.error("Employee service unavailable for employeeId: {}", employeeId, e);
            throw new ServiceUnavailableException("Employee service is unavailable. Please try again later.");
        }

        Hazard hazard = new Hazard();
        hazard.setHazardDescription(dto.getHazardDescription());
        hazard.setHazardLocation(dto.getHazardLocation());
        hazard.setHazardDate(LocalDate.now());
        hazard.setHazardStatus(HazardStatus.PENDING);
        hazard.setEmployeeId(employeeId);

        hazardRepository.save(hazard);
        dto.setHazardId(hazard.getHazardId());
        dto.setEmployeeId(employeeId);
        dto.setHazardStatus(hazard.getHazardStatus());

        log.info("Hazard created successfully for employeeId: {}", employeeId);
        log.info("Hazard created successfully with id: {}", hazard.getHazardId());

        // Send notifications
        try {
            List<Long> adminIds = userClient.getUserIdsByRole("ADMIN");
            List<Long> hazardOfficerIds = userClient.getUserIdsByRole("HAZARD_OFFICER");

            String message = "A new hazard has been reported.";
            
            for (Long adminId : adminIds) {
                notificationClient.createNotification(adminId, hazard.getHazardId(), message, NotificationCategory.HAZARD_REPORTED);
            }
            
            for (Long officerId : hazardOfficerIds) {
                notificationClient.createNotification(officerId, hazard.getHazardId(), message, NotificationCategory.HAZARD_REPORTED);
            }
            log.info("Notifications sent successfully for new hazard: {}", hazard.getHazardId());
        } catch (Exception e) {
            log.error("Failed to send notifications for new hazard: {}", hazard.getHazardId(), e);
        }

        return dto;
    }

    @Override
    @Transactional
    public HazardRequestDto updateHazard(Long hazardId, Long employeeId, String role, HazardRequestDto dto) {

        log.info("Updating hazardId: {} by employeeId: {}, role: {}", hazardId, employeeId, role);

        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(() -> {
                    log.warn("Hazard not found for update. hazardId: {}", hazardId);
                    return new HazardNotFoundException(hazardId);
                });

        boolean isAuthorizedStaff = "ADMIN".equalsIgnoreCase(role) ||
                "HAZARD_OFFICER".equalsIgnoreCase(role) || "SAFETY_OFFICER".equalsIgnoreCase(role);
        boolean isOwner = hazard.getEmployeeId().equals(employeeId);

        if (!isAuthorizedStaff && !isOwner) {
            log.warn("Unauthorized update attempt for hazardId: {}", hazardId);
            throw new InvalidEmployeeException("Not owner of hazard");
        }

        if (hazard.getHazardStatus() != HazardStatus.PENDING && !isAuthorizedStaff) {
            log.warn("Update blocked for hazardId: {} due to status: {}", hazardId, hazard.getHazardStatus());
            throw new IncidentAlreadyReportedException("Hazard is already processed.");
        }

        if (dto.getHazardDescription() != null)
            hazard.setHazardDescription(dto.getHazardDescription());
        if (dto.getHazardLocation() != null)
            hazard.setHazardLocation(dto.getHazardLocation());
        if (dto.getHazardStatus() != null)
            hazard.setHazardStatus(dto.getHazardStatus());

        hazardRepository.save(hazard);

        dto.setEmployeeId(hazard.getEmployeeId());
        dto.setHazardStatus(hazard.getHazardStatus());
        log.info("Hazard updated successfully. hazardId: {}", hazardId);
        return dto;
    }

    @Override
    @Transactional
    public String deleteHazard(Long hazardId, Long employeeId, String role) {

        log.info("Delete request for hazardId: {} by employeeId: {} with role: {}", hazardId, employeeId, role);

        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(() -> new HazardNotFoundException(hazardId));

        // 1. New Business Rule: Check if an incident exists for this hazard
        // Even an ADMIN should generally not delete a hazard that has an active
        // incident report
        if (hazard.getIncident() != null) {
            log.warn("Deletion blocked: Hazard {} has an associated incident.", hazardId);
            throw new IncidentAlreadyReportedException(
                    "Incident has been reported for this hazard and it cannot be deleted.");
        }

        // 2. Define Permissions
        boolean isAuthorizedStaff = "ADMIN".equalsIgnoreCase(role) ||
                "HAZARD_OFFICER".equalsIgnoreCase(role);
        boolean isOwner = hazard.getEmployeeId().equals(employeeId);

        // 3. Permission Check
        if (!isAuthorizedStaff && !isOwner) {
            log.warn("Access Denied: User {} attempted to delete hazard {}", employeeId, hazardId);
            throw new InvalidEmployeeException("Access denied: You do not have permission to delete this record.");
        }

        // 4. Status Check
        if (hazard.getHazardStatus() != HazardStatus.PENDING && !isAuthorizedStaff) {
            throw new IncidentAlreadyReportedException("Only pending hazards can be deleted by users.");
        }

        hazardRepository.delete(hazard);
        return "Hazard deleted successfully";
    }

    @Override
    public List<HazardReportProjection> getHazardsByEmployee(Long employeeId) {
        return hazardRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<HazardReportProjection> getHazardsByStatus(HazardStatus status) {
        return hazardRepository.findByHazardStatus(status);
    }

    @Override
    public Map<String, Long> getHazardSummary() {
        long total = hazardRepository.count();
        long pending = hazardRepository.findByHazardStatus(HazardStatus.PENDING).size();
        long completed = hazardRepository.findByHazardStatus(HazardStatus.COMPLETED).size();

        Map<String, Long> summary = new HashMap<>();
        summary.put("totalHazards", total);
        summary.put("pendingHazards", pending);
        summary.put("completedHazards", completed);
        return summary;
    }

}