package com.cts.hazard_incident_service.controller;

import com.cts.hazard_incident_service.dto.HazardRequestDto;
import com.cts.hazard_incident_service.enums.HazardStatus;
import com.cts.hazard_incident_service.enums.NotificationCategory;
import com.cts.hazard_incident_service.feignClient.NotificationClient;
import com.cts.hazard_incident_service.feignClient.UserClient;
import com.cts.hazard_incident_service.projection.HazardReportProjection;
import com.cts.hazard_incident_service.service.IHazardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hazard")
@Slf4j
public class HazardController {

    @Autowired
    private IHazardService hazardService;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private UserClient userClient;


    @PostMapping("/postHazard")
    public ResponseEntity<HazardRequestDto> addHazard(
            @RequestBody HazardRequestDto request) {

        // Get the employeeId from the DTO (set by Employee Service)
        Long employeeId = request.getEmployeeId();

        log.info("Hazard report received for employeeId: {}", employeeId);

        HazardRequestDto savedHazard = hazardService.addHazard(employeeId, request);

        // Notification Logic...
        try {
            List<Long> hazardOfficerIds = userClient.getUserIdsByRole("HAZARD_OFFICER");
            for (Long officerId : hazardOfficerIds) {
                notificationClient.createNotification(
                        officerId,
                        employeeId,
                        "New hazard reported: " + request.getHazardDescription(),
                        NotificationCategory.HAZARD_REPORTED
                );
            }
        } catch (Exception e) {
            log.warn("Notification failed: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHazard);
    }

    /** Hazard Officer / Admin: view all hazards */
    @GetMapping("/getAllHazard")
    public ResponseEntity<List<HazardReportProjection>> getAllHazards() {
        return ResponseEntity.ok(hazardService.getAllHazards());
    }

    /** Hazard Officer / Admin / Employee: view hazard by ID */
    @GetMapping("/getById/{hazardId}")
    public ResponseEntity<HazardReportProjection> findByHazardId(@PathVariable Long hazardId) {
        return ResponseEntity.ok(hazardService.getHazardById(hazardId));
    }

    @DeleteMapping("/{hazardId}")
    public ResponseEntity<String> deleteHazard(
            @PathVariable Long hazardId,
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestHeader("X-User-Role") String role) {

        Long currentUserId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(hazardService.deleteHazard(hazardId, currentUserId, role));
    }

    @PutMapping("/update/{hazardId}")
    public ResponseEntity<HazardRequestDto> updateHazard(
            @PathVariable Long hazardId,
            @RequestHeader("X-User-Id") Long employeeId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody HazardRequestDto hazardRequestDto) {

        log.info("Update hazard request. hazardId: {}, userId: {}, role: {}", hazardId, employeeId, role);

        HazardRequestDto updated = hazardService.updateHazard(hazardId, employeeId, role, hazardRequestDto);

        // Notify the employee that their hazard was updated
        try {
            if ("HAZARD_OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                notificationClient.createNotification(
                        employeeId,
                        hazardId,
                        "Your hazard report #" + hazardId + " has been updated by a Hazard Officer.",
                        NotificationCategory.HAZARD_UPDATED
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send hazard update notification: {}", e.getMessage());
        }

        return ResponseEntity.ok(updated);
    }

    /** Employee: view own hazards; Hazard Officer/Admin: view any employee's hazards */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<HazardReportProjection>> getHazardsByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(hazardService.getHazardsByEmployee(employeeId));
    }

    /** Hazard Officer / Admin: filter hazards by status */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<HazardReportProjection>> getHazardsByStatus(
            @PathVariable HazardStatus status) {
        return ResponseEntity.ok(hazardService.getHazardsByStatus(status));
    }

    /** Hazard Officer / Admin: summary stats */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getHazardSummary() {
        return ResponseEntity.ok(hazardService.getHazardSummary());
    }
}
