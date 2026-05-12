package com.cts.employee_service.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Catches Validation Errors (e.g., @Email, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 2. Catches Duplicate Email / Database Constraint Errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
            errorResponse.put("email", "This email is already registered. Please login or use another email.");
        } else {
            errorResponse.put("error", "Database conflict occurred.");
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT); // 409 Conflict
    }

    // 3. Catch RuntimeExceptions (e.g., Login "Invalid Credentials" or Custom errors)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Checking if it's a "Not Found" error to return 404, else 401
        HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(ex.getMessage(), status);
    }
}