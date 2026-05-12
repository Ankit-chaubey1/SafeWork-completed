package com.cts.user_service.controller;

import com.cts.user_service.dto.LoginRequestDTO;
import com.cts.user_service.dto.UserPublicDTO;
import com.cts.user_service.dto.UserUpdateDTO;
import com.cts.user_service.entity.User;
import com.cts.user_service.entity.UserAuditLog;
import com.cts.user_service.enums.UserRole;
import com.cts.user_service.repository.UserRepository;
import com.cts.user_service.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(IUserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /** Public: Register any user (Admin, Safety Officer, Hazard Officer, Compliance Officer) */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        log.info("Request to register user: {}", user.getUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(user));
    }

    /** Public: Login for ALL roles — returns JWT token */
    @PostMapping("/login")
    public ResponseEntity<UserPublicDTO> login(@RequestBody LoginRequestDTO request) {
        log.info("Login request for: {}", request.getEmail());
        return ResponseEntity.ok(userService.loginUser(request.getEmail(), request.getPassword()));
    }

    /**
     * Internal endpoint: Called by Employee Service via Feign (no JWT required).
     * Creates a User record when an Employee registers.
     */
    @PostMapping("/internal/create")
    public ResponseEntity<UserPublicDTO> createUserInternal(@RequestBody UserPublicDTO user) {
        log.info("Internal create user: {}", user.getUserEmail());
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    /**
     * Internal endpoint: Returns list of userIds for a given role.
     * Used by Hazard/Training services to find who to notify.
     */
    @GetMapping("/internal/byRole")
    public ResponseEntity<List<Long>> getUserIdsByRole(@RequestParam String role) {
        log.info("Internal query for users with role: {}", role);
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        List<Long> ids = userRepository.findByUserRole(userRole)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }

    @PutMapping("/internal/approve/{userId}")
    public ResponseEntity<UserPublicDTO> approveUserInternal(@PathVariable Long userId) {
        log.info("Internal approve user: {}", userId);
        return ResponseEntity.ok(userService.approveUser(userId));
    }

    @PutMapping("/internal/update/{userId}")
    public ResponseEntity<UserPublicDTO> updateUserInternal(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO dto) {
        log.info("Internal update user: {}", userId);
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    /** Admin only: Update any user's details */
    @PatchMapping("/updateUser/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPublicDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO dto) {
        log.info("Update user ID: {}", userId);
        return ResponseEntity.ok(userService.updateUser(userId, dto));
    }

    /** Admin only: Delete a user */
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        log.info("Delete user ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    /** Admin only: Get user by ID */
    @GetMapping("/getUserById/{userId}")

    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /** Admin only: Get all users */
    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserPublicDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** Admin only: Get user by email */
    @GetMapping("/getByEmail/{userEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@PathVariable String userEmail) {
        return ResponseEntity.ok(userService.getUserByEmail(userEmail));
    }

    /** Admin only: Get user by name */
    @GetMapping("/getByName/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPublicDTO> getUserByName(@PathVariable String userName) {
        return ResponseEntity.ok(userService.getUserByName(userName));
    }

    /** Admin only: Get all audit logs */
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAuditLog>> getAllAuditLogs() {
        log.info("Request to fetch all audit logs");
        return ResponseEntity.ok(userService.getAllAuditLogs());
    }
}
