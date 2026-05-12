package com.cts.compliance_audit_service.externalService;

import com.cts.compliance_audit_service.dto.UserPublicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", path = "/users")
public interface UserClient {

    @GetMapping("/getUserById/{userId}")
    UserPublicDTO getUserById(@PathVariable("userId") Long userId);
}