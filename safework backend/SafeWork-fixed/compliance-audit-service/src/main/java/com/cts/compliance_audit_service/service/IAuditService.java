package com.cts.compliance_audit_service.service;

import com.cts.compliance_audit_service.entity.Audit;
import com.cts.compliance_audit_service.enums.AuditScope;
import com.cts.compliance_audit_service.enums.AuditStatus;
import com.cts.compliance_audit_service.projection.AuditByIdProjection;

import java.util.List;
import java.util.Optional;

public interface IAuditService {

    void createAudit(Audit audit, Long userId);

    List<Audit> getAllAudits();

    Optional<AuditByIdProjection> getAuditById(Long id);

    Audit updateAudit(Long id, Audit updatedAudit);

    void deleteAudit(Long id);

    List<Audit> findByAuditStatus(AuditStatus auditStatus);

    List<Audit> findByAuditScope(AuditScope auditScope);

    List<Audit> findAuditByOfficerId(Long userId);
}
