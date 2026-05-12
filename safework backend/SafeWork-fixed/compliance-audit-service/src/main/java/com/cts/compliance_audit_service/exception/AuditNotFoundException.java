package com.cts.compliance_audit_service.exception;

public class AuditNotFoundException extends RuntimeException {

    public AuditNotFoundException(String message) {
        super(message);
    }
}