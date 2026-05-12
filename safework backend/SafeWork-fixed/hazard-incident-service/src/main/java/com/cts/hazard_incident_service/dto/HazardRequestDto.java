package com.cts.hazard_incident_service.dto;

import com.cts.hazard_incident_service.enums.HazardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class HazardRequestDto {

    private Long hazardId;
    private Long employeeId;
    private String hazardDescription;
    private String hazardLocation;
    private HazardStatus hazardStatus;

}
