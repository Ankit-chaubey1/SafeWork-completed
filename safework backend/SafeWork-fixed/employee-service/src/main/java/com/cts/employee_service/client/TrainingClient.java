package com.cts.employee_service.client;

import com.cts.employee_service.dto.TrainingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "PROGRAM-TRAINING-SERVICE")
public interface TrainingClient {
    @GetMapping("/trainings/mytrainings/{employeeId}")
    List<TrainingDTO> getTrainingsByEmployee(@PathVariable("employeeId") long employeeId);

    @GetMapping("/trainings/getalltrainings")
    List<TrainingDTO> getAllTrainings();
}
