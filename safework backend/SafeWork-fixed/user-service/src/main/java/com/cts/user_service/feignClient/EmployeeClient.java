package com.cts.user_service.feignClient;

import com.cts.user_service.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to call Employee Service.
 * Used to fetch employee data when needed.
 */
@FeignClient(name = "EMPLOYEE-SERVICE")
public interface EmployeeClient {

    @GetMapping("/employees/{id}")
    EmployeeDTO getEmployeeById(@PathVariable long id);
}
