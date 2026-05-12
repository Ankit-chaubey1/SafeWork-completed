package com.cts.user_service.exception;

public class UserNotFoundException extends UserException {
    
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND", 404);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId, "USER_NOT_FOUND", 404);
    }
}
