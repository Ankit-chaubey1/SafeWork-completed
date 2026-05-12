package com.cts.compliance_audit_service.exception;

public class NoAuditFoundException extends RuntimeException {

    public NoAuditFoundException(String message) {
        super(message);
    }
}