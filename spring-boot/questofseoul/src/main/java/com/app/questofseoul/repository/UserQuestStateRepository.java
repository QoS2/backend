package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserQuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQuestStateRepository extends JpaRepository<UserQuestState, UUID> {
    Optional<UserQuestState> findByUserIdAndQuestId(UUID userId, UUID questId);
    
    List<UserQuestState> findByUserId(UUID userId);
}
