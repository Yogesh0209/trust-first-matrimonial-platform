package com.tfmsp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Bureau;

@Repository
public interface BureauRepository extends JpaRepository<Bureau, Long> {
    Optional<Bureau> findByBureauCode(String bureauCode);

    boolean existsByBureauCode(String bureauCode);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bureau b where b.bureauCode = :code")
    Optional<Bureau> findByBureauCodeForUpdate(@Param("code") String code);

    java.util.List<Bureau> findByShareMembersTrueAndStatus(String status);
}
