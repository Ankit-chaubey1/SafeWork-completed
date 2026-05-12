package com.cts.program_training_service.client;

import com.cts.program_training_service.config.FeignConfig;
import com.cts.program_training_service.dto.EmployeeResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Update EmployeeClient.java in the Program-Training-Service
@FeignClient(name = "EMPLOYEE-SERVICE", configuration = FeignConfig.class)
public interface EmployeeClient {

    // Point to the /internal path to skip header validation
    @GetMapping("/employees/internal/{id}")
    EmployeeResponseDTO getEmployeeById(@PathVariable("id") Long id);
}