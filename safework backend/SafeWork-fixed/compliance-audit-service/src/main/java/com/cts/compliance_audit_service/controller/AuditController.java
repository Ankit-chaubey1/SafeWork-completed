package com.cts.compliance_audit_service.controller;
import com.cts.compliance_audit_service.entity.Audit;
import com.cts.compliance_audit_service.enums.AuditScope;
import com.cts.compliance_audit_service.enums.AuditStatus;
import com.cts.compliance_audit_service.projection.AuditByIdProjection;
import com.cts.compliance_audit_service.service.IAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private IAuditService auditService;

    @PostMapping("/createAudit")
    public ResponseEntity<String> createAudit(
            @Valid @RequestBody Audit audit,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request
    ) {
        log.info("Request received to create audit");

        System.out.println("userId: " + userId);
        audit.setOfficerId(userId);

        auditService.createAudit(audit,userId);
        log.info("Audit created successfully");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Audit created successfully with AuditId:"+audit.getAuditId());
    }

    @PutMapping("/updateAudit/{auditId}")
    public ResponseEntity<Audit> updateAudit(@PathVariable Long auditId, @Valid @RequestBody Audit updatedAudit) {
        log.info("Updating audit with id {}", auditId);
        Audit audit = auditService.updateAudit(auditId, updatedAudit);
        log.info("Audit {} updated successfully", auditId);
        return ResponseEntity.ok(audit);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Audit>> getAllAudits() {
        log.info("Fetching all audits");
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    @GetMapping("/getAuditById/{auditId}")
    public ResponseEntity<Optional<AuditByIdProjection>> getAuditById(@PathVariable Long auditId) {
        log.info("Fetching audit with id {}", auditId);
        return ResponseEntity.ok(auditService.getAuditById(auditId));
    }

    @GetMapping("/getAuditByStatus/{status}")
    public ResponseEntity<List<Audit>> getAuditByStatus(@PathVariable AuditStatus status) {
        log.info("Fetching audits with status {}", status);
        return ResponseEntity.ok(auditService.findByAuditStatus(status));
    }

    @GetMapping("/getAuditByScope/{scope}")
    public ResponseEntity<List<Audit>> getAuditByScope(@PathVariable AuditScope scope) {
        log.info("Fetching audits with scope {}", scope);
        return ResponseEntity.ok(auditService.findByAuditScope(scope));
    }

    @GetMapping("/getAuditByOfficer/{userId}")
    public ResponseEntity<List<Audit>> findAuditByOfficer_UserId(@PathVariable Long userId) {
        log.info("Finding audits for officer userId {}", userId);
        return ResponseEntity.ok(auditService.findAuditByOfficerId(userId));
    }

    @DeleteMapping("/deleteAudit/{auditId}")
    public ResponseEntity<String> deleteAudit(@PathVariable Long auditId) {
        log.info("Deleting audit with id {}", auditId);
        auditService.deleteAudit(auditId);
        return ResponseEntity.ok("Audit deleted successfully");
    }
}