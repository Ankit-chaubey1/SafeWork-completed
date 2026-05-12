package com.cts.program_training_service.entity;

import com.cts.program_training_service.enums.ProgramStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long programId;

    private String programTitle;
    private String programDescription;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate programStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate programEndDate;
    @Enumerated(EnumType.STRING)
    private ProgramStatus programStatus;
}
