package com.cts.user_service.exception;

public class UserAlreadyExistsException extends UserException {
    
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists", "USER_ALREADY_EXISTS", 409);
    }
}
