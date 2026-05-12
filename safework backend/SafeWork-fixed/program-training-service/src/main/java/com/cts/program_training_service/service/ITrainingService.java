package com.cts.program_training_service.service;

import com.cts.program_training_service.dto.EmployeeResponseDTO;
import com.cts.program_training_service.entity.Training;

import java.util.List;

public interface ITrainingService {
    List<Training> getAllTrainings();
    Training getTrainingById(Long id);
    Training createTraining(Training training);
    Training updateTraining(Long id, Training training);
    void deleteTraining(Long id);
    List<Training> getTrainingsByEmployee(Long employeeId);
    EmployeeResponseDTO getEmployeeDetails(Long employeeId);
}
