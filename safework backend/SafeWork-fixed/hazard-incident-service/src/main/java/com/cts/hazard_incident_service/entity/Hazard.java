package com.cts.hazard_incident_service.entity;

import com.cts.hazard_incident_service.enums.HazardStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hazard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long hazardId;

    private String hazardDescription;
    private String hazardLocation;
    private LocalDate hazardDate;

    @Enumerated(EnumType.STRING)
    private HazardStatus hazardStatus;

//    @OneToOne(mappedBy = "hazard")
//    @JsonBackReference(value = "hazard-incident")
//    private Incident incident;

    // In Hazard.java
    @OneToOne(mappedBy = "hazard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value = "hazard-incident")
    private Incident incident;

    private Long employeeId;
}