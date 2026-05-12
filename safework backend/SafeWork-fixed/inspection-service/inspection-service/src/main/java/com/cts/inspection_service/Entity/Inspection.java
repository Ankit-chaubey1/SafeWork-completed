package com.cts.inspection_service.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.Date;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inspectionId;

    private String inspectionLocation;

    // Add these fields to match the DTO names
    private String inspectionFindings;
    private Date inspectionDate;

    private String inspectionStatus;
    private Long officerId;
    private String officerName;
    private String officerEmail;
}