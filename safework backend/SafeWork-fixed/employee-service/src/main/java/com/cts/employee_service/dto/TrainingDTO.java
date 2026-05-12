package com.cts.employee_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {
    private Long trainingId;
    private String trainingName;
    private String status;
    private LocalDate completionDate;
    private Long employeeId;
}