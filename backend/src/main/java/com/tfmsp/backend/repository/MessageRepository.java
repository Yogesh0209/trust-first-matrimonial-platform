package com.tfmsp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tfmsp.backend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByInterestIdOrderBySentAtAsc(Long interestId);

    Optional<Message> findFirstByInterestIdOrderBySentAtDesc(Long interestId);

    long countByInterestId(Long interestId);
}
