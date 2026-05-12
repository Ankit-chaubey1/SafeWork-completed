package com.cts.inspection_service.Dto;



import lombok.Data;
import java.util.Date;

@Data
public class ComplianceResponseDTO {
    private Long checkId;
    private String complianceCheckResult;
    private String complianceCheckNotes;
    private Date complianceCheckDate;
    private String complianceCheckStatus;
    private Long inspectionId;
}