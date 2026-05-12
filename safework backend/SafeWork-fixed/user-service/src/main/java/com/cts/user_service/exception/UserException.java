package com.cts.user_service.exception;

public class UserException extends RuntimeException {
    private String errorCode;
    private int status;
    
    public UserException(String message) {
        super(message);
        this.errorCode = "USER_ERROR";
        this.status = 400;
    }
    
    public UserException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = 400;
    }
    
    public UserException(String message, String errorCode, int status) {
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
