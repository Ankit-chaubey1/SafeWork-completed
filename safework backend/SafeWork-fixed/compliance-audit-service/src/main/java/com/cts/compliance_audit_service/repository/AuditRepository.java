package com.cts.compliance_audit_service.repository;

import com.cts.compliance_audit_service.entity.Audit;
import com.cts.compliance_audit_service.enums.AuditScope;
import com.cts.compliance_audit_service.enums.AuditStatus;
import com.cts.compliance_audit_service.projection.AuditByIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    List<Audit> findByAuditStatus(AuditStatus auditStatus);

    List<Audit> findByAuditScope(AuditScope auditScope);

    List<Audit> findByOfficerId(Long officerId);

    Optional<AuditByIdProjection> findProjectedByAuditId(Long auditId);
}