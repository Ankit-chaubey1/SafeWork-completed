package com.cts.inspection_service.FleignClient;

import com.cts.inspection_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", configuration = FeignConfig.class)
public interface UserClient {
    @GetMapping("/users/getUserById/{userId}")
    UserPublicDto getUserById(@PathVariable Long userId);
}