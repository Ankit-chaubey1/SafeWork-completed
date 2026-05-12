package com.cts.inspection_service.controller;

import com.cts.inspection_service.Dto.ComplianceRequestDTO;
import com.cts.inspection_service.Dto.ComplianceResponseDTO;
import com.cts.inspection_service.Service.IComplianceCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compliance-checks")
public class ComplianceCheckController {

    @Autowired
    private IComplianceCheckService service;

    @PostMapping("/createCheck")
    public ResponseEntity<ComplianceResponseDTO> createCheck(
            @RequestBody ComplianceRequestDTO dto,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(service.createCheck(dto, requesterId, role));
    }

    @GetMapping("/getAllChecks")
    public ResponseEntity<List<ComplianceResponseDTO>> getAllChecks() {
        return ResponseEntity.ok(service.getAllChecks());
    }

    @GetMapping("/getChecksByInspectionId/{inspectionId}")
    public ResponseEntity<List<ComplianceResponseDTO>> getChecksByInspectionId(@PathVariable Long inspectionId) {
        return ResponseEntity.ok(service.getChecksByInspectionId(inspectionId));
    }

    @PutMapping("/updateCheck/{checkId}")
    public ResponseEntity<ComplianceResponseDTO> updateCheck(
            @PathVariable Long checkId,
            @RequestBody ComplianceRequestDTO details,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(service.updateCheck(checkId, details, requesterId, role));
    }

    @DeleteMapping("/deleteCheck/{checkId}")
    public ResponseEntity<String> deleteCheck(
            @PathVariable Long checkId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String role) {
        service.deleteCheck(checkId, requesterId, role);
        return ResponseEntity.ok("Deleted Successfully");
    }
}
