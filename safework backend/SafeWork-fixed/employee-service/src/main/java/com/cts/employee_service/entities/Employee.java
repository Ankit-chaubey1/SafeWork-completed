package com.cts.employee_service.entities;

import com.cts.employee_service.enums.EmployeeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @Column(name = "employee_id")

    private long employeeId;




    @Column(name = "email", unique = true, nullable = false)
    private String email;


    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;


    @Column(name = "employee_name")
    private String employeeName;


    @Column(name = "employee_dob")
    private LocalDate employeeDOB;

    @Column(name = "gender")
    private String employeeGender;

    @Column(name = "address", length = 500)
    private String employeeAddress;

    @Column(name = "contact_number")
    private String employeeContact;

    @Column(name = "department_name")
    private String employeeDepartmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeStatus employeeStatus;

    @Embedded
    @Valid
    private EmployeeDocument document;


}