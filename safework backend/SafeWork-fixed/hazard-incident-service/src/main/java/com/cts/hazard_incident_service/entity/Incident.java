package com.cts.hazard_incident_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long incidentId;

    private String action;
    private LocalDate incidentDate;

    private Long officerId;

    @OneToOne
    @JoinColumn(name = "hazard_id", referencedColumnName = "hazardId")
    private Hazard hazard;
}
