package com.tfmsp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tfmsp.backend.entity.ProfilePhoto;

public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {

    List<ProfilePhoto> findByProfileIdOrderByIsPrimaryDescUploadedAtDesc(Long profileId);

    Optional<ProfilePhoto> findByIdAndProfileId(Long id, Long profileId);

    Optional<ProfilePhoto> findByProfileIdAndIsPrimaryTrue(Long profileId);

    long countByProfileId(Long profileId);
}
