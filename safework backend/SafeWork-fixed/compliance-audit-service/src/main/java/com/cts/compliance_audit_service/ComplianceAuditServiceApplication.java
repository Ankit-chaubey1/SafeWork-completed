package com.cts.compliance_audit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.cts.compliance_audit_service.externalService")
public class  ComplianceAuditServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComplianceAuditServiceApplication.class, args);
	}
}