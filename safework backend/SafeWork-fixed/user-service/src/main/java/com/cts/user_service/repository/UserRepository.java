package com.cts.user_service.repository;

import com.cts.user_service.entity.User;
import com.cts.user_service.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserName(String userName);
    List<User> findByUserRole(UserRole userRole);
}
