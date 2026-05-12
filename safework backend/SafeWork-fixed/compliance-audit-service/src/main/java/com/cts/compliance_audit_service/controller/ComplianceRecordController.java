package com.cts.compliance_audit_service.controller;

import com.cts.compliance_audit_service.entity.ComplianceRecord;
import com.cts.compliance_audit_service.enums.ComplianceEntityType;
import com.cts.compliance_audit_service.enums.ComplianceResult;
import com.cts.compliance_audit_service.service.IComplianceRecordService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/complianceRecord")
public class ComplianceRecordController {

    private final IComplianceRecordService complianceRecordService;

    @Autowired
    public ComplianceRecordController(IComplianceRecordService complianceRecordService) {
        this.complianceRecordService = complianceRecordService;
    }

    @PostMapping("/createComplianceRecord")

    public ResponseEntity<String> createComplianceRecord(@Valid @RequestBody ComplianceRecord complianceRecord) {
        log.info("Request received to create ComplianceRecord");
        complianceRecordService.createComplianceRecord(complianceRecord);
        log.info("ComplianceRecord created successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Compliance Record created successfully with ComplianceId:"+complianceRecord.getComplianceId());
    }

    @PutMapping("/updateComplianceRecord/{complianceId}")

    public ResponseEntity<String> updateComplianceRecord(
            @PathVariable Long complianceId,
            @Valid @RequestBody ComplianceRecord updatedRecord) {

        log.info("Request received to update ComplianceRecord with ID {}", complianceId);
        complianceRecordService.updateComplianceRecord(complianceId, updatedRecord);
        log.info("ComplianceRecord {} updated successfully", complianceId);
        return ResponseEntity.ok("Compliance Record updated successfully");
    }

    @DeleteMapping("/deleteComplianceRecord/{complianceId}")

    public ResponseEntity<String> deleteComplianceRecord(@PathVariable Long complianceId) {
        log.warn("Request received to delete ComplianceRecord with ID {}", complianceId);
        complianceRecordService.deleteComplianceRecord(complianceId);
        log.info("ComplianceRecord {} deleted successfully", complianceId);
        return ResponseEntity.ok("Compliance Record deleted successfully");
    }

    @GetMapping("/getAllComplianceRecord")

    public ResponseEntity<List<ComplianceRecord>> getAllComplianceRecords(@RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Request received to fetch all ComplianceRecords:"+userId);
        List<ComplianceRecord> records = complianceRecordService.getAllComplianceRecords();
        log.info("Fetched {} ComplianceRecords", records.size());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/getComplianceRecordById/{complianceId}")

    public ResponseEntity<?> getComplianceRecordById(@PathVariable Long complianceId) {
        log.info("Request received to fetch ComplianceRecord with ID {}", complianceId);
        Optional<ComplianceRecord> recordOpt = complianceRecordService.getComplianceRecordById(complianceId);
        return ResponseEntity.ok(recordOpt.get());
    }

    @GetMapping("/ComplianceEntityType/{entityType}")

    public ResponseEntity<List<ComplianceRecord>> findByEntityType(
            @PathVariable ComplianceEntityType entityType) {
        log.info("Request received to fetch ComplianceRecords by EntityType {}", entityType);
        return ResponseEntity.ok(complianceRecordService.findByEntityType(entityType));
    }

    @GetMapping("/findByComplianceResult/{complianceResult}")

    public ResponseEntity<List<ComplianceRecord>> findByComplianceResult(
            @PathVariable ComplianceResult complianceResult) {
        log.info("Request received to fetch ComplianceRecords by Result {}", complianceResult);
        return ResponseEntity.ok(complianceRecordService.findByComplianceResult(complianceResult));
    }
}