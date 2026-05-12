package com.cts.program_training_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrainingNotFoundException extends RuntimeException {
    public TrainingNotFoundException(Long id) {
        super("Training not found with id " + id);
    }
}