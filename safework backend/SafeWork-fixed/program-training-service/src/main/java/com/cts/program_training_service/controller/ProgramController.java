package com.cts.program_training_service.controller;

import com.cts.program_training_service.entity.Program;
import com.cts.program_training_service.service.IProgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/programs")
@Slf4j
public class ProgramController {

    private final IProgramService service;

    @Autowired
    public ProgramController(IProgramService service) {
        this.service = service;
    }

    /** View all programs: Headers injected by Gateway */
    @GetMapping("/getallprograms")
    public ResponseEntity<List<Program>> getAllPrograms(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Request by UserID: {} with Role: {} to fetch all programs", userId, role);

        // Even though Gateway checks roles, you can add extra safety check here if needed:
        // if (!"ADMIN".equals(role) && !"SAFETY_OFFICER".equals(role)) { return Forbidden... }

        return ResponseEntity.ok(service.getAllPrograms());
    }

    /** View a specific program */
    @GetMapping("/getprogrambyid/{id}")
    public ResponseEntity<Program> getProgramById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("User {} ({}) fetching program ID: {}", userId, role, id);
        return ResponseEntity.ok(service.getProgramById(id));
    }

    /** Create a training program */
    @PostMapping("/createprogram")
    public ResponseEntity<Object> createProgram(
            @RequestBody Program program,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Admin/User {} creating program: {}", userId, program.getProgramTitle());

        Program saved = service.createProgram(program);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Program created successfully",
                "data", saved
        ));
    }

    /** Update a program */
    @PutMapping("/updateprogrambyid/{id}")
    public ResponseEntity<Object> updateProgram(
            @PathVariable Long id,
            @RequestBody Program program,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Update attempt on ID: {} by User: {}", id, userId);

        Program updated = service.updateProgram(id, program);
        return ResponseEntity.ok(Map.of(
                "message", "Program updated successfully",
                "data", updated
        ));
    }

    /** Delete a program */
    @DeleteMapping("/deleteprogrambyid/{id}")
    public ResponseEntity<Object> deleteProgram(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("Delete request for ID: {} by User: {} (Role: {})", id, userId, role);

        service.deleteProgram(id);
        return ResponseEntity.ok(Map.of("message", "Program deleted successfully"));
    }
}