package com.tfmsp.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Subscription;
import com.tfmsp.backend.enums.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByBureauId(Long bureauId);

    Optional<Subscription> findFirstByBureauIdAndStatusOrderByEndDateDesc(Long bureauId, SubscriptionStatus status);
    
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    List<Subscription> findByStatusAndEndDate(SubscriptionStatus status, LocalDate date);
}
