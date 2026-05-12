package com.cts.employee_service.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequest {
    //    private Long userId; // This will be null when sending, but populated when receiving
    private String userName;
    private String userEmail;
    private String userContact;
    private String userStatus;
    private String password;
    private String userRole;

    private java.time.LocalDate employeeDOB;
    private String employeeGender;
    private String employeeAddress;
    private String employeeDepartmentName;

    private String employeeDocumentType;
    private String employeeFileURL;

    private LocalDate uploadedDate;


    private String verificationStatus;


}
