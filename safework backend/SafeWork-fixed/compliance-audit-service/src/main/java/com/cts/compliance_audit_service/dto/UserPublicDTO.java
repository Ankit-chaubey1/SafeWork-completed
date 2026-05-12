package com.cts.compliance_audit_service.dto;

import lombok.Data;

@Data
public class UserPublicDTO {
    private Long userId;
    private String userName;
    private String userEmail;
}