package com.cts.hazard_incident_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IncidentRequestDto{

    @NotBlank(message = "Incident action must not be empty")
    @Size(min = 5, max = 255, message = "Incident action must be between 5 and 255 characters")
    private String action;


}
