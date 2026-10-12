package com.tfmsp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Shortlist;
import com.tfmsp.backend.entity.ShortlistId;

@Repository
public interface ShortlistRepository extends JpaRepository<Shortlist, ShortlistId> {
    List<Shortlist> findByIdProfileIdOrderByCreatedAtDesc(Long profileId);

    boolean existsById(ShortlistId id);
}
