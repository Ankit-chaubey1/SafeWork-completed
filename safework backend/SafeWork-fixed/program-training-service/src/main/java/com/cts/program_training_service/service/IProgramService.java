package com.cts.program_training_service.service;

import com.cts.program_training_service.entity.Program;

import java.util.List;

public interface IProgramService {
    List<Program> getAllPrograms();
    Program getProgramById(Long id);
    Program createProgram(Program program);
    Program updateProgram(Long id, Program program);
    void deleteProgram(Long id);
}