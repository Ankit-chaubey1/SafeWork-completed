package com.cts.inspection_service.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "compliance_checks")
public class ComplianceCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkId;

    private String complianceCheckResult;
    private String complianceCheckNotes;
    private Date complianceCheckDate;
    private String complianceCheckStatus;

    // In a microservice, we often store just the ID of the related entity
    // to avoid deep nesting issues during JSON serialization.
    private Long inspectionId;
}