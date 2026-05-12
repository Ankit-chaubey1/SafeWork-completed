
package com.cts.employee_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicDto {
    private Long userId;
    private String userEmail;
    private String userName;
    private String userContact;
    private String userStatus;

}