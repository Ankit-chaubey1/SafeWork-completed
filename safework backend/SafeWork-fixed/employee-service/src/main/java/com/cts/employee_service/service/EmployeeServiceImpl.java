
package com.cts.employee_service.service;

import com.cts.employee_service.client.HazardClient;
import com.cts.employee_service.client.TrainingClient;
import com.cts.employee_service.client.UserClient;
import com.cts.employee_service.dto.*;
import com.cts.employee_service.entities.Employee;
import com.cts.employee_service.entities.EmployeeDocument;
import com.cts.employee_service.enums.EmployeeStatus;
import com.cts.employee_service.repositories.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeServiceImpl implements IEmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private HazardClient hazardClient;
    @Autowired
    private TrainingClient trainingClient;
    @Autowired
    private UserClient userClient;

    private EmployeeResponseDTO mapToDTO(Employee emp) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setEmployeeId(emp.getEmployeeId());
        dto.setEmployeeName(emp.getEmployeeName());
        dto.setEmail(emp.getEmail());
        dto.setEmployeeDepartmentName(emp.getEmployeeDepartmentName());
        dto.setEmployeeStatus(emp.getEmployeeStatus().toString());
        return dto;
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));
        return mapToDTO(emp);
    }

    @Override
    public UserPublicDto registerEmployee(EmployeeRequest employeeRequest, MultipartFile file) {
        // 1. Validation: Ensure email is present
        if (employeeRequest.getUserEmail() == null || employeeRequest.getUserEmail().isBlank()) {
            throw new RuntimeException("Registration failed: Employee email is mandatory.");
        }

        log.info("Starting registration process for: {}", employeeRequest.getUserEmail());

        if (employeeRepository.findByEmail(employeeRequest.getUserEmail()).isPresent()) {
            throw new RuntimeException("Employee with email " + employeeRequest.getUserEmail() + " already exists");
        }

        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
                File dir = new File(uploadDir);
                if (!dir.exists())
                    dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(file.getInputStream(), filePath);

                log.info("File saved to: {}", filePath.toString());
                employeeRequest.setEmployeeFileURL(fileName); // Store filename or relative path
            } catch (IOException e) {
                log.error("Failed to save file", e);
                throw new RuntimeException("Could not save uploaded file: " + e.getMessage());
            }
        }

        // 2. Set default onboarding status: admin must approve before login
        employeeRequest.setUserRole("EMPLOYEE");
        employeeRequest.setUserStatus("PENDING");

        // 3. External API Call
        // This sends the EmployeeRequest to User-Service.
        // Ensure your UserClient @PostMapping consumes EmployeeRequest correctly.
        UserPublicDto userResponse = userClient.createUser(employeeRequest);

        if (userResponse == null || userResponse.getUserId() == null) {
            throw new RuntimeException("User Service failed to return a valid User ID.");
        }

        // Safety: never overwrite an existing employee row because of user-id
        // collisions.
        if (employeeRepository.existsById(userResponse.getUserId())) {
            throw new RuntimeException(
                    "Registration failed: User ID conflict detected. Please restart user-service with persistent DB config.");
        }

        // 4. Local Persistence via helper method
        try {
            return saveLocalEmployee(employeeRequest, userResponse);
        } catch (Exception e) {
            log.error("Failed to save employee locally: {}. Data inconsistency exists with User service.",
                    e.getMessage());
            throw e;
        }
    }

    @Transactional
    public UserPublicDto saveLocalEmployee(EmployeeRequest request, UserPublicDto response) {
        Employee emp = new Employee();

        // Sync ID with User Service (validated for collisions in registerEmployee)
        emp.setEmployeeId(response.getUserId());
        emp.setEmail(request.getUserEmail());
        emp.setEmployeeName(request.getUserName());
        emp.setEmployeeContact(request.getUserContact());
        emp.setPassword(request.getPassword()); // Storing encoded or masked password if required
        emp.setEmployeeStatus(EmployeeStatus.PENDING);
        emp.setEmployeeDOB(request.getEmployeeDOB());
        emp.setEmployeeGender(request.getEmployeeGender());
        emp.setEmployeeAddress(request.getEmployeeAddress());
        emp.setEmployeeDepartmentName(request.getEmployeeDepartmentName());

        // Handle Embedded Document
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployeeDocumentType(request.getEmployeeDocumentType());
        doc.setEmployeeFileURL(request.getEmployeeFileURL());
        doc.setUploadedDate(LocalDate.now());
        doc.setVerificationStatus("PENDING");

        emp.setDocument(doc);

        employeeRepository.save(emp);
        log.info("Employee local record saved for ID: {}", emp.getEmployeeId());

        return response;
    }

    @Override
    public String loginEmployee(String email, String password) {
        throw new UnsupportedOperationException(
                "Login is handled by User Service. Use POST /users/login.");
    }

    @Override
    public HazardDTO reportHazard(HazardDTO hazardDTO) {
        log.info("Forwarding report for Employee: {}", hazardDTO.getEmployeeId());
        return hazardClient.createHazard(hazardDTO);
    }

    @Override
    public List<HazardDTO> getHazardsByEmployee(long employeeId) {
        if (!employeeRepository.existsById(employeeId))
            throw new RuntimeException("Employee not found.");
        return hazardClient.getHazardsByEmployee(employeeId);
    }

    @Override
    public List<TrainingDTO> getTrainingsByEmployee(long employeeId) {
        if (!employeeRepository.existsById(employeeId))
            throw new RuntimeException("Employee not found.");
        return trainingClient.getTrainingsByEmployee(employeeId);
    }

    @Override
    public EmployeeDocument getEmployeeDocument(long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found."));
        return emp.getDocument();
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployeeDocument(long id, EmployeeDocument newDoc) {
        log.info("Updating document for employee ID: {}", id);

        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

        if (newDoc != null) {
            newDoc.setVerificationStatus("PENDING");
            if (newDoc.getUploadedDate() == null) {
                newDoc.setUploadedDate(LocalDate.now());
            }
            emp.setDocument(newDoc);
        }

        Employee saved = employeeRepository.save(emp);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO approveEmployee(long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        emp.setEmployeeStatus(EmployeeStatus.ACTIVE);
        Employee saved = employeeRepository.save(emp);
        userClient.approveUser(employeeId);

        log.info("Employee approved successfully. Employee ID: {}", employeeId);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(long employeeId, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        if (request.getUserName() != null)
            emp.setEmployeeName(request.getUserName());
        if (request.getUserEmail() != null)
            emp.setEmail(request.getUserEmail().trim().toLowerCase());
        if (request.getUserContact() != null)
            emp.setEmployeeContact(request.getUserContact());
        if (request.getEmployeeDOB() != null)
            emp.setEmployeeDOB(request.getEmployeeDOB());
        if (request.getEmployeeGender() != null)
            emp.setEmployeeGender(request.getEmployeeGender());
        if (request.getEmployeeAddress() != null)
            emp.setEmployeeAddress(request.getEmployeeAddress());
        if (request.getEmployeeDepartmentName() != null)
            emp.setEmployeeDepartmentName(request.getEmployeeDepartmentName());
        if (request.getUserStatus() != null)
            emp.setEmployeeStatus(toEmployeeStatus(request.getUserStatus()));

        Employee saved = employeeRepository.save(emp);

        UserUpdateDTO userUpdate = new UserUpdateDTO();
        userUpdate.setUserName(saved.getEmployeeName());
        userUpdate.setUserEmail(saved.getEmail());
        userUpdate.setUserContact(saved.getEmployeeContact());
        userUpdate.setUserStatus(saved.getEmployeeStatus().name());
        userClient.updateUser(saved.getEmployeeId(), userUpdate);

        log.info("Employee {} updated successfully", employeeId);
        return mapToDTO(saved);
    }

    private EmployeeStatus toEmployeeStatus(String status) {
        try {
            return EmployeeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EmployeeStatus.ACTIVE;
        }
    }
}