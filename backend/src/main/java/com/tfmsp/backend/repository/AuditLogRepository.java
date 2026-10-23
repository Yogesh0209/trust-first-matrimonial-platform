package com.tfmsp.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    // logs written by anyone who belongs to this bureau (members and the host)
    @Query("select a from AuditLog a "
            + "where a.actorUserId in (select u.id from User u where u.bureauId = :bureauId) "
            + "order by a.createdAt desc")
    List<AuditLog> findByBureau(@Param("bureauId") Long bureauId, Pageable pageable);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
}