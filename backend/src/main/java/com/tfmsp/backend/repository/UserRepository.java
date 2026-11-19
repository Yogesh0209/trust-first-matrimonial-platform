package com.tfmsp.backend.repository;

import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByBureauId(Long bureauId);
    
    long countByBureauIdAndRole(Long bureauId, com.tfmsp.backend.enums.Role role);

    java.util.List<User> findByBureauIdAndRole(Long bureauId, com.tfmsp.backend.enums.Role role);
    
    java.util.List<User> findByStatusAndLastActiveAtBefore(UserStatus status, LocalDateTime before);
}
