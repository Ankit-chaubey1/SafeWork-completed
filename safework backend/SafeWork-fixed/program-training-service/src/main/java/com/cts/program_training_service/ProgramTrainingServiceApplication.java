package com.cts.program_training_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ProgramTrainingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgramTrainingServiceApplication.class, args);
    }
}
