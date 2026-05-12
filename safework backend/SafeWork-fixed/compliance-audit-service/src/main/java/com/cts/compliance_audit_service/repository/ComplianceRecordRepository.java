package com.cts.compliance_audit_service.repository;


import com.cts.compliance_audit_service.entity.ComplianceRecord;
import com.cts.compliance_audit_service.enums.ComplianceEntityType;
import com.cts.compliance_audit_service.enums.ComplianceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord,Long> {

    List<ComplianceRecord> findByEntityType(ComplianceEntityType entityType);
    List<ComplianceRecord> findByComplianceResult(ComplianceResult complianceResult);

}
