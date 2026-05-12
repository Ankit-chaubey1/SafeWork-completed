package com.cts.inspection_service.FleignClient;

import lombok.Data;

@Data
public class UserPublicDto {
    private Long userId;
    private String userName;
    private String userEmail;
    private String userContact;
}
