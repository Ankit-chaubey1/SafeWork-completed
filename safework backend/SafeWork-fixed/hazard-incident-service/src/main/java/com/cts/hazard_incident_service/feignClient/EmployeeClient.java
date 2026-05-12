package com.cts.hazard_incident_service.feignClient;

import com.cts.hazard_incident_service.dto.EmployeePublicDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Must match spring.application.name = EMPLOYEE-SERVICE exactly
@FeignClient(name = "EMPLOYEE-SERVICE")
public interface EmployeeClient {

    @GetMapping("/employees/{employeeId}")
    EmployeePublicDto getEmployeeById(@PathVariable("employeeId") Long employeeId);
}
