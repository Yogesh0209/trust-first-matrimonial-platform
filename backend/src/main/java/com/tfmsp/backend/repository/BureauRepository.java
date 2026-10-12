package com.tfmsp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Bureau;

@Repository
public interface BureauRepository extends JpaRepository<Bureau, Long> {
    Optional<Bureau> findByBureauCode(String bureauCode);

    boolean existsByBureauCode(String bureauCode);

    java.util.List<Bureau> findByShareMembersTrueAndStatus(String status);
}
