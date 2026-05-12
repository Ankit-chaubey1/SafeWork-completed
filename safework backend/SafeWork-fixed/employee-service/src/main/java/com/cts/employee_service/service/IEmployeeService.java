package com.cts.employee_service.service;

import com.cts.employee_service.dto.*;
import com.cts.employee_service.entities.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IEmployeeService {
    UserPublicDto registerEmployee(EmployeeRequest employee, MultipartFile file);
    String loginEmployee(String email, String password);
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO getEmployeeById(long id);
    EmployeeDocument getEmployeeDocument(long employeeId);

    // Hazard (External Service via Feign)
    HazardDTO reportHazard(HazardDTO hazardDTO);
    List<HazardDTO> getHazardsByEmployee(long employeeId);

    // Training (External Service via Feign)
    List<TrainingDTO> getTrainingsByEmployee(long employeeId);

    EmployeeResponseDTO updateEmployeeDocument(long id, EmployeeDocument document);
    EmployeeResponseDTO approveEmployee(long employeeId);
    EmployeeResponseDTO updateEmployee(long employeeId, EmployeeRequest request);
}