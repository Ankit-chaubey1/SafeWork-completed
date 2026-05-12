package com.cts.user_service.service;

import com.cts.user_service.dto.UserPublicDTO;
import com.cts.user_service.dto.UserUpdateDTO;
import com.cts.user_service.entity.User;
import com.cts.user_service.entity.UserAuditLog;

import java.util.List;

public interface IUserService {
    UserPublicDTO loginUser(String email, String password);
    User registerUser(User user);
    UserPublicDTO createUser(UserPublicDTO userDto);
    UserPublicDTO updateUser(Long userId, UserUpdateDTO dto);
    void deleteUser(Long userId);
    UserPublicDTO getUserById(Long userId);
    List<UserPublicDTO> getAllUsers();
    UserPublicDTO getUserByEmail(String userEmail);
    UserPublicDTO getUserByName(String userName);
    UserPublicDTO approveUser(Long userId);
    List<UserAuditLog> getAllAuditLogs();
}
