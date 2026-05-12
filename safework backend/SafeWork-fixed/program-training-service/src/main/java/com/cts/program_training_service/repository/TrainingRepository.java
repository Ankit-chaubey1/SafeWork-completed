package com.cts.program_training_service.repository;

import com.cts.program_training_service.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByEmployeeId(Long employeeId);
}
