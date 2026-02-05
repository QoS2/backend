package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserQuestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserQuestHistoryRepository extends JpaRepository<UserQuestHistory, UUID> {
    List<UserQuestHistory> findByUserIdAndQuestIdOrderByCreatedAt(UUID userId, UUID questId);
    
    List<UserQuestHistory> findByUserIdAndQuestIdAndNodeIdOrderByCreatedAt(
        UUID userId, UUID questId, UUID nodeId);
}
