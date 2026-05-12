package com.cts.employee_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.time.LocalDate;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDocument {

    @Column(name = "doc_type")
    private String employeeDocumentType;

    @Column(name = "file_url")
    private String employeeFileURL;

    @Column(name = "uploaded_date")
    private LocalDate uploadedDate;

    @Column(name = "verification_status")
    private String verificationStatus;
}