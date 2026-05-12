package com.cts.employee_service.dto;

import lombok.Data;

@Data
public class EmployeeResponseDTO {
    private long employeeId;
    private String employeeName;
    private String email;
    private String employeeDepartmentName;
    private String employeeStatus;


}