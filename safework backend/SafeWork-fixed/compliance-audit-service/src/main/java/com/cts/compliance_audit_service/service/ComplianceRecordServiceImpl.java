package com.cts.compliance_audit_service.service;

import com.cts.compliance_audit_service.entity.ComplianceRecord;
import com.cts.compliance_audit_service.enums.ComplianceEntityType;
import com.cts.compliance_audit_service.enums.ComplianceResult;
import com.cts.compliance_audit_service.exception.ResourceNotFoundException;
import com.cts.compliance_audit_service.repository.ComplianceRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ComplianceRecordServiceImpl implements IComplianceRecordService {

    private final ComplianceRecordRepository complianceRecordRepository;

    @Autowired
    public ComplianceRecordServiceImpl(ComplianceRecordRepository complianceRecordRepository) {
        this.complianceRecordRepository = complianceRecordRepository;
    }

    @Override
    public void createComplianceRecord(ComplianceRecord complianceRecord) {
        log.info("Saving ComplianceRecord");
        complianceRecordRepository.save(complianceRecord);
        log.info("ComplianceRecord saved successfully");
    }

    @Override
    public List<ComplianceRecord> getAllComplianceRecords() {
        log.info("Fetching all ComplianceRecords from database");
        List<ComplianceRecord> records = complianceRecordRepository.findAll();

        if (records.isEmpty()) {
            log.warn("No ComplianceRecords found");
            throw new ResourceNotFoundException("No compliance records found");
        }

        return records;
    }

    @Override
    public Optional<ComplianceRecord> getComplianceRecordById(Long id) {
        log.info("Fetching ComplianceRecord with ID {}", id);

        Optional<ComplianceRecord> record = complianceRecordRepository.findById(id);

        if (record.isEmpty()) {
            log.error("ComplianceRecord not found with ID {}", id);
            throw new ResourceNotFoundException("ComplianceRecord not found with id " + id);
        }

        return record;
    }

    @Override
    public void updateComplianceRecord(Long id, ComplianceRecord updatedRecord) {
        log.info("Updating ComplianceRecord with ID {}", id);

        ComplianceRecord existing = complianceRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ComplianceRecord not found with ID {}", id);
                    return new ResourceNotFoundException(
                            "ComplianceRecord not found with id " + id);
                });

        existing.setEntityId(updatedRecord.getEntityId());
        existing.setEntityType(updatedRecord.getEntityType());
        existing.setComplianceResult(updatedRecord.getComplianceResult());
        existing.setComplianceDate(updatedRecord.getComplianceDate());
        existing.setComplianceNotes(updatedRecord.getComplianceNotes());

        complianceRecordRepository.save(existing);
        log.info("ComplianceRecord {} updated successfully", id);
    }

    @Override
    public void deleteComplianceRecord(Long id) {
        log.warn("Deleting ComplianceRecord with ID {}", id);

        if (!complianceRecordRepository.existsById(id)) {
            log.error("ComplianceRecord not found with ID {}", id);
            throw new ResourceNotFoundException(
                    "ComplianceRecord not found with id " + id);
        }

        complianceRecordRepository.deleteById(id);
        log.info("ComplianceRecord {} deleted successfully", id);
    }

    @Override
    public List<ComplianceRecord> findByEntityType(ComplianceEntityType entityType) {
        log.info("Fetching ComplianceRecords by EntityType {}", entityType);

        List<ComplianceRecord> records = complianceRecordRepository.findByEntityType(entityType);

        if (records.isEmpty()) {
            log.warn("No ComplianceRecords found for EntityType {}", entityType);
            throw new ResourceNotFoundException(
                    "No records found for entity type: " + entityType);
        }

        return records;
    }

    @Override
    public List<ComplianceRecord> findByComplianceResult(ComplianceResult complianceResult) {
        log.info("Fetching ComplianceRecords by ComplianceResult {}", complianceResult);

        List<ComplianceRecord> records = complianceRecordRepository.findByComplianceResult(complianceResult);

        if (records.isEmpty()) {
            log.warn("No ComplianceRecords found for Result {}", complianceResult);
            throw new ResourceNotFoundException(
                    "No records found for result: " + complianceResult);
        }

        return records;
    }
}
