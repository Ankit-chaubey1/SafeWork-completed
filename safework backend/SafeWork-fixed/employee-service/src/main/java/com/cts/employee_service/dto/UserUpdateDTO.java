package com.cts.employee_service.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String userName;
    private String userEmail;
    private String userContact;
    private String userStatus;
}
