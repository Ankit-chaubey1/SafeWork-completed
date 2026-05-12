package com.cts.user_service.exception;

public class CustomException extends RuntimeException {
    private String errorCode;
    private int status;
    
    public CustomException(String message) {
        super(message);
        this.errorCode = "CUSTOM_ERROR";
        this.status = 400;
    }
    
    public CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = 400;
    }
    
    public CustomException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getStatus() {
        return status;
    }
}

