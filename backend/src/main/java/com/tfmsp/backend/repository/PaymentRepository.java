package com.tfmsp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySubscriptionId(Long subscriptionId);

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}
