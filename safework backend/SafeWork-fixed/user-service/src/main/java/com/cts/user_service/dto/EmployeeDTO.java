package com.cts.user_service.dto;

import lombok.Data;

@Data
public class EmployeeDTO {
    private long employeeId;
    private String employeeName;
    private String email;
    private String employeeDepartmentName;
    private String employeeStatus;
}
