package com.cts.employee_service.dto;

import lombok.Data;

@Data
public class HazardDTO {
    private Long employeeId;
    private String hazardDescription;
    private String hazardLocation;
    private String hazardStatus;
    private Long hazardId;
}
