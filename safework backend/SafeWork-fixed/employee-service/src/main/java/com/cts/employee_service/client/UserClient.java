package com.cts.employee_service.client;

import com.cts.employee_service.dto.EmployeeRequest;
import com.cts.employee_service.dto.UserPublicDto;
import com.cts.employee_service.dto.UserUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @PostMapping("/users/internal/create")
    UserPublicDto createUser(@RequestBody EmployeeRequest employee);

    @PutMapping("/users/internal/approve/{userId}")
    UserPublicDto approveUser(@PathVariable("userId") Long userId);

    @PutMapping("/users/internal/update/{userId}")
    UserPublicDto updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateDTO dto);

}
