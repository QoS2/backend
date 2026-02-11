package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatTurn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {
    List<ChatTurn> findBySessionIdOrderByTurnIdxAsc(Long sessionId);
    long countBySessionId(Long sessionId);
}
