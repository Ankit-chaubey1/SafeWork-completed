package com.cts.compliance_audit_service.projection;

import com.cts.compliance_audit_service.enums.AuditScope;
import com.cts.compliance_audit_service.enums.AuditStatus;

import java.time.LocalDate;

public interface AuditByIdProjection {
    AuditScope getAuditScope();
    String getAuditFinding();
    LocalDate getAuditDate();
    AuditStatus getAuditStatus();
}
