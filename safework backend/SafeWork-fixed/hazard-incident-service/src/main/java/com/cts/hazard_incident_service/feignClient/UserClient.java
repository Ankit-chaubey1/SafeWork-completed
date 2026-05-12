package com.cts.hazard_incident_service.feignClient;

import com.cts.hazard_incident_service.dto.UserPublicDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/users/internal/byRole")
    List<Long> getUserIdsByRole(@RequestParam String role);


    @GetMapping("/users//getUserById/{officerId}")
    void getUserById(@RequestParam Long officerId);


}
