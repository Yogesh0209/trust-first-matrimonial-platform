package com.tfmsp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerProfileIdAndBlockedProfileId(Long blockerProfileId, Long blockedProfileId);

    List<Block> findByBlockerProfileId(Long blockerProfileId);

    // pass the same id twice: returns blocks where I am on either side
    List<Block> findByBlockerProfileIdOrBlockedProfileId(Long a, Long b);

    void deleteByBlockerProfileIdAndBlockedProfileId(Long blockerProfileId, Long blockedProfileId);
}