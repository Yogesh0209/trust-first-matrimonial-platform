package com.tfmsp.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    List<Interest> findByStatusAndCreatedAtBefore(InterestStatus status, LocalDateTime before);
    
    @Query("select count(i) from Interest i where i.status = :status "
            + "and (i.senderProfileId in :ids or i.receiverProfileId in :ids)")
    long countByStatusForProfiles(@Param("status") InterestStatus status,
            @Param("ids") Collection<Long> ids);
    
    @Query("select i from Interest i where i.status = :status "
            + "and (i.senderProfileId = :profileId or i.receiverProfileId = :profileId) order by i.id")
    List<Interest> findByStatusForProfile(@Param("status") InterestStatus status,
            @Param("profileId") Long profileId);
}
