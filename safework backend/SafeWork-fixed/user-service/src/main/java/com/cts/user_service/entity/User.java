package com.cts.user_service.entity;

import com.cts.user_service.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    private String userName;
    private String userEmail;
    private String userContact;
    private String userStatus;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;



}
