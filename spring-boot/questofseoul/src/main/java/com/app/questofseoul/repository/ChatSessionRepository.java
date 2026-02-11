package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatSession;
import com.app.questofseoul.domain.enums.ChatRefType;
import com.app.questofseoul.domain.enums.SessionKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByTourRunIdOrderByCreatedAtAsc(Long tourRunId);
    Optional<ChatSession> findByTourRunIdAndContextRefTypeAndContextRefId(Long tourRunId, ChatRefType refType, Long refId);
    Optional<ChatSession> findByTourRunIdAndSessionKind(Long tourRunId, SessionKind sessionKind);
}
