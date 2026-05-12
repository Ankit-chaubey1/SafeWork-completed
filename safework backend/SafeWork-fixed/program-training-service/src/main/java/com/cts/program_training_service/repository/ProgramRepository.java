package com.cts.program_training_service.repository;

import com.cts.program_training_service.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

}