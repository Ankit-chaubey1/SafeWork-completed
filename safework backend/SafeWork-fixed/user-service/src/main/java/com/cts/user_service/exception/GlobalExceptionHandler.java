package com.cts.user_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatus())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        log.error("User already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatus())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex, WebRequest request) {
        log.error("User exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatus())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .error("VALIDATION_ERROR")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(validationErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, WebRequest request) {
        log.error("Custom exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatus())
                .message(ex.getMessage())
                .error(ex.getErrorCode())
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .error("INTERNAL_SERVER_ERROR")
                .timestamp(System.currentTimeMillis())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
