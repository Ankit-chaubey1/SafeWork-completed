package com.cts.hazard_incident_service.dto;

import com.cts.hazard_incident_service.enums.UserRole;
import lombok.Data;

@Data
public class UserPublicDto {

    private Long userId;
    private String userName;
    private String userEmail;
    private UserRole userRole;

}
