package com.tfmsp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfmsp.backend.entity.Profile;
import com.tfmsp.backend.enums.Visibility;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Profile> findByIdAndVisibility(Long id, Visibility visibility);

    java.util.List<Profile> findByVisibility(Visibility visibility);

    java.util.List<Profile> findByUserIdInAndVisibility(java.util.List<Long> userIds, Visibility visibility);
}
