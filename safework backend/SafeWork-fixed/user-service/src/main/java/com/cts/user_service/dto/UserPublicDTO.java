package com.cts.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Hides null fields in JSON response
public class UserPublicDTO {

    private Long userId;        // Database ID
    private String userName;
    private String userEmail;
    private String password;    // Raw password during registration; set to null before returning
    private String userContact;
    private String userStatus;
    private String userRole;    // String format for easy JSON transfer
    private String token;       // JWT Token (only populated during login)
}