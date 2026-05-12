package com.cts.user_service.service;

import com.cts.user_service.dto.UserPublicDTO;
import com.cts.user_service.dto.UserUpdateDTO;
import com.cts.user_service.entity.User;
import com.cts.user_service.entity.UserAuditLog;
import com.cts.user_service.repository.UserAuditLogRepository;
import com.cts.user_service.enums.UserRole;
import com.cts.user_service.exception.CustomException;
import com.cts.user_service.repository.UserRepository;
import com.cts.user_service.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserAuditLogRepository auditLogRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public UserPublicDTO loginUser(String email, String password) {
        log.info("Login attempt for email: {}", email);

        User user = userRepository.findByUserEmail(email.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Login failed: No user found with email {}", email);
                    return new CustomException("Login Failed: Invalid credentials");
                });

        if (!"ACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            log.warn("Login blocked: user {} is not approved yet (status={})", email, user.getUserStatus());
            throw new CustomException("Your account is pending admin approval. Please wait for activation.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: Password mismatch for email {}", email);
            throw new CustomException("Login Failed: Invalid credentials");
        }

        log.info("User {} authenticated successfully with role: {}", email, user.getUserRole());

        UserPublicDTO dto = toDTO(user);
        String token = jwtUtil.generateToken(
                user.getUserEmail(),
                user.getUserRole().name(),
                user.getUserId()
        );
        dto.setToken(token);
        return dto;
    }

    @Override
    public User registerUser(User user) {
        log.info("Registering new user with email: {}", user.getUserEmail());

        String normalizedEmail = user.getUserEmail().trim().toLowerCase();
        if (userRepository.findByUserEmail(normalizedEmail).isPresent()) {
            log.warn("Registration failed: Email {} already exists", normalizedEmail);
            throw new CustomException("User with this email already exists");
        }

        user.setUserEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserStatus("ACTIVE");
        User savedUser = userRepository.save(user);
        log.info("User successfully registered with ID: {}", savedUser.getUserId());
        return savedUser;
    }

    /**
     * Called internally by Employee Service via Feign.
     * Receives raw password and encodes it before saving.
     */
//    @Transactional
//    @Override
//    public UserPublicDTO createUser(UserPublicDTO userDto) {
//        log.info("Creating user record from Employee Service for: {}", userDto.getUserEmail());
//
//        String normalizedEmail = userDto.getUserEmail().trim().toLowerCase();
//        if (userRepository.findByUserEmail(normalizedEmail).isPresent()) {
//            log.warn("User with email {} already exists, skipping creation", normalizedEmail);
//            // Return existing user DTO instead of throwing
//            User existing = userRepository.findByUserEmail(normalizedEmail).get();
//            return toDTO(existing);
//        }
//
//        User user = new User();
//        user.setUserName(userDto.getUserName());
//        user.setUserEmail(normalizedEmail);
//        // Raw password arrives from Employee Service - encode it here
//        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
//        user.setUserRole(UserRole.valueOf(userDto.getUserRole()));
//        user.setUserContact(userDto.getUserContact());
//        user.setUserStatus("ACTIVE");
//
//        User savedUser = userRepository.save(user);
//        log.info("User record created with ID: {}", savedUser.getUserId());
//        return toDTO(savedUser);
//    }

    @Transactional
    @Override
    public UserPublicDTO createUser(UserPublicDTO userDto) {
        // 1. Validation & Logging
        if (userDto == null || userDto.getUserEmail() == null) {
            log.error("Create User failed: UserPublicDTO or Email is null. Received: {}", userDto);
            throw new CustomException("User email is required for account creation.");
        }

        log.info("Creating user record for: {}", userDto.getUserEmail());

        // 2. Normalization (Safe from NPE now)
        String normalizedEmail = userDto.getUserEmail().trim().toLowerCase();

        // 3. Idempotency Check
        if (userRepository.findByUserEmail(normalizedEmail).isPresent()) {
            log.warn("User with email {} already exists, skipping creation", normalizedEmail);
            User existing = userRepository.findByUserEmail(normalizedEmail).get();
            return toDTO(existing);
        }

        // 4. Entity Mapping
        User user = new User();
        user.setUserName(userDto.getUserName());
        user.setUserEmail(normalizedEmail);

        // Ensure password isn't null before encoding
        String rawPassword = (userDto.getPassword() != null) ? userDto.getPassword() : "Default@123";
        user.setPassword(passwordEncoder.encode(rawPassword));

        // Handle potential null Role
        String roleStr = (userDto.getUserRole() != null) ? userDto.getUserRole() : "EMPLOYEE";
        user.setUserRole(UserRole.valueOf(roleStr.toUpperCase()));

        user.setUserContact(userDto.getUserContact());
        user.setUserStatus(userDto.getUserStatus() != null ? userDto.getUserStatus().toUpperCase() : "PENDING");

        User savedUser = userRepository.save(user);
        log.info("User record created with ID: {}", savedUser.getUserId());
        return toDTO(savedUser);
    }

    @Override
    public UserPublicDTO updateUser(Long userId, UserUpdateDTO dto) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with id: " + userId));

        if (dto.getUserName() != null) user.setUserName(dto.getUserName());
        if (dto.getUserEmail() != null) user.setUserEmail(dto.getUserEmail().trim().toLowerCase());
        if (dto.getUserContact() != null) user.setUserContact(dto.getUserContact());
        if (dto.getUserStatus() != null) user.setUserStatus(dto.getUserStatus().toUpperCase());
        if (dto.getUserRole() != null) user.setUserRole(dto.getUserRole());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        log.info("User ID {} successfully updated", userId);
        return toDTO(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new CustomException("User not found");
        }
        userRepository.deleteById(userId);
        log.info("User ID {} deleted successfully", userId);
    }

    @Override
    public UserPublicDTO getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::toDTO)
                .orElseThrow(() -> new CustomException("User not found with id: " + userId));
    }

    @Override
    public List<UserPublicDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserPublicDTO getUserByEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail.trim().toLowerCase())
                .map(this::toDTO)
                .orElseThrow(() -> new CustomException("User not found with email: " + userEmail));
    }

    @Override
    public UserPublicDTO getUserByName(String userName) {
        return userRepository.findByUserName(userName)
                .map(this::toDTO)
                .orElseThrow(() -> new CustomException("User not found with name: " + userName));
    }

    @Override
    @Transactional
    public UserPublicDTO approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with id: " + userId));
        user.setUserStatus("ACTIVE");
        userRepository.save(user);
        log.info("User {} approved and activated", userId);
        return toDTO(user);
    }

    @Override
    public List<UserAuditLog> getAllAuditLogs() {
        log.info("Fetching all user audit logs");
        return auditLogRepository.findAll();
    }

    private UserPublicDTO toDTO(User user) {
        UserPublicDTO dto = new UserPublicDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserContact(user.getUserContact());
        dto.setUserStatus(user.getUserStatus());
        dto.setUserRole(user.getUserRole().name());
        return dto;
    }
}
