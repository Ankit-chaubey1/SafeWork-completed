package com.cts.compliance_audit_service.entity;


import com.cts.compliance_audit_service.enums.ComplianceEntityType;
import com.cts.compliance_audit_service.enums.ComplianceResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long complianceId;

    @Column(nullable = true, length = 100)
    private String complianceTitle = "";

    @Column(name = "audit_id", nullable = true)
    private Long auditId = 0L;

    @Column(name = "entity_id", nullable = false)
    private Long entityId = 0L;

    @NotNull(message = "Entity type is required")
    @Enumerated(EnumType.STRING)
    private ComplianceEntityType entityType;

    @NotNull(message = "Compliance result is required")
    @Enumerated(EnumType.STRING)
    private ComplianceResult complianceResult;

    @NotNull(message = "Compliance date is required")
    @PastOrPresent(message = "Compliance date cannot be in the future")
    private LocalDate complianceDate;

    @NotBlank(message = "Compliance notes must not be blank")
    @Size(max = 500, message = "Compliance notes must not exceed 500 characters")
    private String complianceNotes;
}