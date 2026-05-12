package com.cts.program_training_service.controller;

import com.cts.program_training_service.client.NotificationClient;
import com.cts.program_training_service.dto.EmployeeResponseDTO;
import com.cts.program_training_service.entity.Training;
import com.cts.program_training_service.service.ITrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trainings")
@Slf4j
public class TrainingController {

    private final ITrainingService service;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    public TrainingController(ITrainingService service) {
        this.service = service;
    }

    /** Admin / Safety Officer: get all trainings */
    @GetMapping("/getalltrainings")
    public ResponseEntity<List<Training>> getAllTrainings(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("User {} ({}) fetching all trainings", userId, role);
        return ResponseEntity.ok(service.getAllTrainings());
    }

    /** Admin / Safety Officer / Employee: get training by ID */
    @GetMapping("/gettrainingbyid/{id}")
    public ResponseEntity<Training> getTrainingById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        log.info("User {} fetching training ID: {}", userId, id);
        return ResponseEntity.ok(service.getTrainingById(id));
    }

    /** Admin only: create a training assignment with validation */
    @PostMapping("/createtraining")
    public ResponseEntity<Object> createTraining(
            @RequestBody Training training,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Request to create training by User: {} with Role: {}", userId, role);

        // 1. Security Check
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Only Administrators can create training records"));
        }

        try {
            // 2. Call Service: Validates Employee ID (Feign) and Program ID (DB)
            Training savedTraining = service.createTraining(training);

            // 3. Trigger Notification
            if (savedTraining.getEmployeeId() != null) {
                try {
                    notificationClient.createNotification(
                            savedTraining.getEmployeeId(),
                            savedTraining.getId(),
                            "A new training program has been assigned to you. Please check your dashboard.",
                            "TRAINING_ASSIGNED"
                    );
                    log.info("Notification sent to Employee ID: {}", savedTraining.getEmployeeId());
                } catch (Exception e) {
                    log.error("Failed to send notification: {}", e.getMessage());
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Training record created successfully",
                    "data", savedTraining
            ));

        } catch (RuntimeException e) {
            // Catches "Employee/Program not found" errors from Service
            log.error("Training creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Admin only: update training with validation */
    @PutMapping("/updatetrainingbyid/{id}")
    public ResponseEntity<Object> updateTraining(
            @PathVariable Long id,
            @RequestBody Training training,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Admin {} updating training ID: {}", userId, id);

        try {
            Training updated = service.updateTraining(id, training);
            return ResponseEntity.ok(Map.of(
                    "message", "Training updated successfully",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Admin only: delete training */
    @DeleteMapping("/deletetrainingbyid/{id}")
    public ResponseEntity<Object> deleteTraining(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Admin {} deleting training ID: {}", userId, id);
        service.deleteTraining(id);
        return ResponseEntity.ok(Map.of("message", "Training deleted successfully"));
    }

    /** Admin / Safety Officer: get employee details */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeDetails(
            @PathVariable Long employeeId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("User {} fetching details for employee: {}", userId, employeeId);
        return ResponseEntity.ok(service.getEmployeeDetails(employeeId));
    }

    /** Employee: get own assigned trainings */
    @GetMapping("/mytrainings/{employeeId}")
    public ResponseEntity<Object> getMyTrainings(
            @PathVariable Long employeeId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        if ("EMPLOYEE".equalsIgnoreCase(role) && !userId.equals(employeeId.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: You can only view your own trainings"));
        }

        log.info("Fetching trainings for employee ID: {}", employeeId);
        return ResponseEntity.ok(service.getTrainingsByEmployee(employeeId));
    }
}