package com.tfmsp.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Interest;
import com.tfmsp.backend.enums.InterestStatus;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findBySenderProfileIdAndReceiverProfileId(Long senderProfileId, Long receiverProfileId);

    boolean existsBySenderProfileIdAndReceiverProfileId(Long senderProfileId, Long receiverProfileId);

    List<Interest> findBySenderProfileIdOrderByCreatedAtDesc(Long senderProfileId);

    List<Interest> findByReceiverProfileIdOrderByCreatedAtDesc(Long receiverProfileId);

    List<Interest> findBySenderProfileIdOrReceiverProfileIdAndStatusOrderByCreatedAtDesc(
            Long senderProfileId, Long receiverProfileId, InterestStatus status);

    long countBySenderProfileIdAndCreatedAtAfter(Long senderProfileId, LocalDateTime after);
}
