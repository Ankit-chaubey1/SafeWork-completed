package com.cts.program_training_service.entity;

import com.cts.program_training_service.enums.TrainingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate trainingCompletionDate;

    @Enumerated(EnumType.STRING)
    private TrainingStatus trainingStatus;

    private Long programId;
    private Long employeeId;
}
