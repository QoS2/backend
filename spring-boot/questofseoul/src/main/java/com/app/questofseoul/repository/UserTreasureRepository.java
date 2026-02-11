package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserTreasure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTreasureRepository extends JpaRepository<UserTreasure, Long> {
    List<UserTreasure> findByUserId(UUID userId);
    boolean existsByUserIdAndTreasureId(UUID userId, Long treasureId);
    Optional<UserTreasure> findByUserIdAndTreasureId(UUID userId, Long treasureId);
}
