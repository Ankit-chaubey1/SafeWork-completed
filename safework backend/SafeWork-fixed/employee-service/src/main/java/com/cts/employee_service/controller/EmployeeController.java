//
//package com.cts.employee_service.controller;
//
//import com.cts.employee_service.dto.EmployeeRequest;
//import com.cts.employee_service.dto.EmployeeResponseDTO;
//import com.cts.employee_service.dto.HazardDTO;
//import com.cts.employee_service.dto.UserPublicDto;
//import com.cts.employee_service.entities.Employee;
//import com.cts.employee_service.entities.EmployeeDocument;
//import com.cts.employee_service.repositories.EmployeeRepository;
//import com.cts.employee_service.service.IEmployeeService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/employees")
//@Slf4j
//public class EmployeeController {
//
//    @Autowired
//    private IEmployeeService employeeService;
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    /**
//     * Public: Register a new Employee.
//     */
//    @PostMapping("/register")
//    public ResponseEntity<UserPublicDto> register(@Valid @RequestBody EmployeeRequest employee) {
//        log.info("Registering new employee: {}", employee.getUserEmail());
//        return new ResponseEntity<>(employeeService.registerEmployee(employee), HttpStatus.CREATED);
//    }
//
//    /** Admin only (Gateway handles role check) */
//    @GetMapping("/getall")
//    public ResponseEntity<List<EmployeeResponseDTO>> getAll() {
//        return ResponseEntity.ok(employeeService.getAllEmployees());
//    }
//
//    /**
//     * Get Employee by ID.
//     * Uses X-User-Role and X-User-Email injected by Gateway for validation.
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getEmployee(
//            @PathVariable("id") long id,
//            @RequestHeader("X-User-Role") String role,
//            @RequestHeader("X-User-Email") String tokenEmail) {
//
//        if ("EMPLOYEE".equals(role)) {
//            Employee emp = employeeRepository.findById(id).orElse(null);
//            if (emp == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
//            }
//            // Self-service check
//            if (!emp.getEmail().equalsIgnoreCase(tokenEmail)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("Access denied: You can only view your own profile");
//            }
//        }
//        return ResponseEntity.ok(employeeService.getEmployeeById(id));
//    }
//
//    /**
//     * Employee only: Report a hazard for themselves.
//     */
//    @PostMapping("/hazard/report")
//    public ResponseEntity<HazardDTO> reportHazard(
//            @Valid @RequestBody HazardDTO hazardDTO,
//            @RequestHeader("X-User-Id") Long userId) { // Read ID directly from Header
//
//        log.info("Reporting hazard for User ID: {}", userId);
//
//        // 1. Set the ID directly from the Header
//        hazardDTO.setEmployeeId(userId);
//
//        // 2. Call service
//        return new ResponseEntity<>(employeeService.reportHazard(hazardDTO), HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{id}/hazards")
//    public ResponseEntity<?> getHazards(
//            @PathVariable long id,
//            @RequestHeader("X-User-Role") String role,
//            @RequestHeader("X-User-Email") String tokenEmail) {
//
//        if ("EMPLOYEE".equals(role) && !isOwnRecord(id, tokenEmail)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: own data only");
//        }
//        return ResponseEntity.ok(employeeService.getHazardsByEmployee(id));
//    }
//
//    @GetMapping("/{id}/trainings")
//    public ResponseEntity<?> getMyTrainings(
//            @PathVariable long id,
//            @RequestHeader("X-User-Role") String role,
//            @RequestHeader("X-User-Email") String tokenEmail) {
//
//        if ("EMPLOYEE".equals(role) && !isOwnRecord(id, tokenEmail)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: own data only");
//        }
//        return ResponseEntity.ok(employeeService.getTrainingsByEmployee(id));
//    }
//
//    // ---- Helper method using headers instead of parsing JWT ----
//
//    private boolean isOwnRecord(long id, String tokenEmail) {
//        Employee emp = employeeRepository.findById(id).orElse(null);
//        return emp != null && emp.getEmail().equalsIgnoreCase(tokenEmail);
//    }
//
//    // Add this to EmployeeController.java in the Employee Service
//    @GetMapping("/internal/{id}")
//    public ResponseEntity<EmployeeResponseDTO> getEmployeeInternal(@PathVariable("id") long id) {
//        log.info("Internal request to fetch employee details for ID: {}", id);
//        return ResponseEntity.ok(employeeService.getEmployeeById(id));
//    }
//
//
//    @PutMapping("/{id}/document")
//    public ResponseEntity<?> updateDocument(
//            @PathVariable long id,
//            @Valid @RequestBody EmployeeDocument document,
//            @RequestHeader("X-User-Role") String role,
//            @RequestHeader("X-User-Email") String tokenEmail) {
//
//        log.info("Document update request for ID: {} by User: {}", id, tokenEmail);
//
//        // AUTHENTICATION CHECK: Only the owner (EMPLOYEE) can update their own document.
//        if ("EMPLOYEE".equalsIgnoreCase(role)) {
//            Employee emp = employeeRepository.findById(id).orElse(null);
//
//            if (emp == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
//            }
//
//            // Check if the email in the DB matches the email from the Token
//            if (!emp.getEmail().equalsIgnoreCase(tokenEmail)) {
//                log.warn("Security Alert: User {} tried to update document of ID {}", tokenEmail, id);
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("Access denied: You can only update your own profile.");
//            }
//        }
//
//        // If check passes (or if user is ADMIN), proceed to update
//        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployeeDocument(id, document);
//        return ResponseEntity.ok(updatedEmployee);
//    }
//
//}
package com.cts.employee_service.controller;

import com.cts.employee_service.dto.EmployeeRequest;
import com.cts.employee_service.dto.EmployeeResponseDTO;
import com.cts.employee_service.dto.HazardDTO;
import com.cts.employee_service.dto.UserPublicDto;
import com.cts.employee_service.entities.Employee;
import com.cts.employee_service.entities.EmployeeDocument;
import com.cts.employee_service.repositories.EmployeeRepository;
import com.cts.employee_service.service.IEmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/employees")
@Slf4j
public class EmployeeController {

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserPublicDto> register(
            @RequestPart("employee") EmployeeRequest employee,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("Registering new employee: {}", employee.getUserEmail());
        return new ResponseEntity<>(employeeService.registerEmployee(employee, file), HttpStatus.CREATED);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<EmployeeResponseDTO>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PatchMapping("/approve/{id}")
    public ResponseEntity<?> approveEmployee(
            @PathVariable("id") long id,
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can approve employees.");
        }
        return ResponseEntity.ok(employeeService.approveEmployee(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable("id") long id,
            @RequestHeader("X-User-Role") String role,
            @RequestBody EmployeeRequest request) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin/manager can update employees.");
        }
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(
            @PathVariable("id") long id,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String tokenEmail) {

        if ("EMPLOYEE".equals(role)) {
            Employee emp = employeeRepository.findById(id).orElse(null);
            if (emp == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }
            if (!emp.getEmail().equalsIgnoreCase(tokenEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: You can only view your own profile");
            }
        }
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping("/hazard/report")
    public ResponseEntity<HazardDTO> reportHazard(
            @Valid @RequestBody HazardDTO hazardDTO,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Reporting hazard for User ID: {}", userId);
        hazardDTO.setEmployeeId(userId);
        return new ResponseEntity<>(employeeService.reportHazard(hazardDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/hazards")
    public ResponseEntity<?> getHazards(
            @PathVariable long id,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String tokenEmail) {

        if ("EMPLOYEE".equals(role) && !isOwnRecord(id, tokenEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: own data only");
        }
        return ResponseEntity.ok(employeeService.getHazardsByEmployee(id));
    }

    @GetMapping("/{id}/trainings")
    public ResponseEntity<?> getMyTrainings(
            @PathVariable long id,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String tokenEmail) {

        if ("EMPLOYEE".equals(role) && !isOwnRecord(id, tokenEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: own data only");
        }
        return ResponseEntity.ok(employeeService.getTrainingsByEmployee(id));
    }

    private boolean isOwnRecord(long id, String tokenEmail) {
        Employee emp = employeeRepository.findById(id).orElse(null);
        return emp != null && emp.getEmail().equalsIgnoreCase(tokenEmail);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeInternal(@PathVariable("id") long id) {
        log.info("Internal request to fetch employee details for ID: {}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}/document")
    public ResponseEntity<?> updateDocument(
            @PathVariable long id,
            @Valid @RequestBody EmployeeDocument document,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String tokenEmail) {

        log.info("Document update request for ID: {} by User: {}", id, tokenEmail);

        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            Employee emp = employeeRepository.findById(id).orElse(null);

            if (emp == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }

            if (!emp.getEmail().equalsIgnoreCase(tokenEmail)) {
                log.warn("Security Alert: User {} tried to update document of ID {}", tokenEmail, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: You can only update your own profile.");
            }
        }

        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployeeDocument(id, document);
        return ResponseEntity.ok(updatedEmployee);
    }
}