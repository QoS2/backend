package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findByTourRunIdAndSpotId(Long tourRunId, Long spotId);
}
