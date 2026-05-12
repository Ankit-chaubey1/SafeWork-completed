package com.cts.program_training_service.service;

import com.cts.program_training_service.entity.Program;
import com.cts.program_training_service.exception.ProgramNotFoundException;
import com.cts.program_training_service.repository.ProgramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProgramServiceImpl implements IProgramService {

    private final ProgramRepository repository;

    @Autowired
    public ProgramServiceImpl(ProgramRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Program> getAllPrograms() {
        log.info("Fetching all programs from database...");
        return repository.findAll();
    }

    @Override
    public Program getProgramById(Long id) {
        log.info("Fetching program with ID: {}", id);
        return repository.findById(id).orElseThrow(() -> new ProgramNotFoundException(id));
    }

    @Override
    public Program createProgram(Program program) {
        log.info("Creating new program: {}", program.getProgramTitle());
        return repository.save(program);
    }

    @Override
    public Program updateProgram(Long id, Program program) {
        log.info("Updating program with ID: {}", id);
        Program existingProgram = getProgramById(id);
        existingProgram.setProgramTitle(program.getProgramTitle());
        existingProgram.setProgramDescription(program.getProgramDescription());
        existingProgram.setProgramStartDate(program.getProgramStartDate());
        existingProgram.setProgramEndDate(program.getProgramEndDate());
        existingProgram.setProgramStatus(program.getProgramStatus());
        return repository.save(existingProgram);
    }

    @Override
    public void deleteProgram(Long id) {
        log.info("Deleting program with ID: {}", id);
        Program program = getProgramById(id);
        if (program != null) {
            repository.delete(program);
        } else {
            throw new ProgramNotFoundException(id);
        }
    }
}