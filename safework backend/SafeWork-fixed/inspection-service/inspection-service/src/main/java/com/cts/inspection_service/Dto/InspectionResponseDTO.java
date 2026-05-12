package com.cts.inspection_service.Dto;


import lombok.Data;
import java.util.Date;

@Data
public class InspectionResponseDTO {
    private Long inspectionId;
    private String inspectionLocation;
    private String inspectionFindings;
    private Date inspectionDate;
    private String inspectionStatus;
    private Long officerId;
}