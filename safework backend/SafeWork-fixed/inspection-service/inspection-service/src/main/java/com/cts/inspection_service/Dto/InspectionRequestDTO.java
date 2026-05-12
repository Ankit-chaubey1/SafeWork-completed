package com.cts.inspection_service.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InspectionRequestDTO {

    @NotNull(message = "Inspection location cannot be null")
    private String inspectionLocation;

    private String inspectionFindings;
    private Date inspectionDate;
    private String inspectionStatus;

    @NotNull(message = "Officer ID is required")
    private Long officerId;
}