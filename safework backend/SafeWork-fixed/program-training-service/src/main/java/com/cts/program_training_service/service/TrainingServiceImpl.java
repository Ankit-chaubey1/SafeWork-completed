package com.cts.program_training_service.service;

import com.cts.program_training_service.client.EmployeeClient;
import com.cts.program_training_service.dto.EmployeeResponseDTO;
import com.cts.program_training_service.entity.Training;
import com.cts.program_training_service.exception.TrainingNotFoundException;
import com.cts.program_training_service.repository.ProgramRepository;
import com.cts.program_training_service.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrainingServiceImpl implements ITrainingService {

    private final TrainingRepository repository;
    private final EmployeeClient employeeClient;
    private final ProgramRepository programRepository;

    @Autowired
    public TrainingServiceImpl(TrainingRepository repository,
                               EmployeeClient employeeClient,
                               ProgramRepository programRepository) {
        this.repository = repository;
        this.employeeClient = employeeClient;
        this.programRepository = programRepository;
    }

    @Override
    public List<Training> getAllTrainings() {
        return repository.findAll();
    }

    @Override
    public Training getTrainingById(Long id) {
        return repository.findById(id).orElseThrow(() -> new TrainingNotFoundException(id));
    }

    @Override
    public Training createTraining(Training training) {
        log.info("Validating IDs before creating training...");
        validateIds(training.getEmployeeId(), training.getProgramId());

        log.info("Saving training for Employee: {}", training.getEmployeeId());
        return repository.save(training);
    }

    @Override
    public Training updateTraining(Long id, Training training) {
        log.info("Validating IDs before updating training ID: {}", id);
        Training existing = getTrainingById(id);

        // Validate IDs again in case they are changed in the update request
        validateIds(training.getEmployeeId(), training.getProgramId());

        existing.setTrainingCompletionDate(training.getTrainingCompletionDate());
        existing.setTrainingStatus(training.getTrainingStatus());
        existing.setProgramId(training.getProgramId());
        existing.setEmployeeId(training.getEmployeeId());

        return repository.save(existing);
    }

    /** Helper method to check if Employee and Program exist */
    private void validateIds(Long employeeId, Long programId) {
        // 1. Check if Program exists in local database
        boolean programExists = programRepository.existsById(programId);
        if (!programExists) {
            log.error("Validation Failed: Program ID {} not found", programId);
            throw new RuntimeException("Validation Error: Program ID " + programId + " does not exist in the system.");
        }

        // 2. Check if Employee exists in Employee Service via Feign
        try {
            EmployeeResponseDTO employee = employeeClient.getEmployeeById(employeeId);
            if (employee == null) {
                throw new RuntimeException("Employee not found");
            }
        } catch (Exception e) {
            // Itha add pannunga, exact error console-la print aagum
            log.error("ACTUAL FEIGN ERROR: ", e);
            throw new RuntimeException("Validation Error: Employee ID " + employeeId + " is invalid or Employee Service is unavailable. Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteTraining(Long id) {
        Training training = getTrainingById(id);
        repository.delete(training);
    }

    @Override
    public List<Training> getTrainingsByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    @Override
    public EmployeeResponseDTO getEmployeeDetails(Long employeeId) {
        return employeeClient.getEmployeeById(employeeId);
    }
}