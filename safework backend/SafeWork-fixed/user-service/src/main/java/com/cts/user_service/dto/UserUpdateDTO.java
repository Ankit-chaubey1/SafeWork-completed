package com.cts.user_service.dto;


import com.cts.user_service.enums.UserRole;
import lombok.Data;

@Data
public class UserUpdateDTO {

    private String userName;
    private String userEmail;
    private String userContact;
    private String userStatus;
    private String password;
    private UserRole userRole;
}