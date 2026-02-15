package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserTreasureStatus;
import com.app.questofseoul.domain.enums.TreasureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserTreasureStatusRepository extends JpaRepository<UserTreasureStatus, Long> {

    List<UserTreasureStatus> findByTourRunId(Long tourRunId);

    Optional<UserTreasureStatus> findByTourRunIdAndTreasureSpotId(Long tourRunId, Long treasureSpotId);

    @Query("SELECT COUNT(t) FROM UserTreasureStatus t WHERE t.tourRun.id = :runId AND t.status = :status")
    long countByTourRunIdAndStatus(Long runId, TreasureStatus status);

    @Query("SELECT t FROM UserTreasureStatus t JOIN FETCH t.treasureSpot s JOIN FETCH t.tourRun r JOIN FETCH r.tour WHERE r.user.id = :userId AND t.status = 'GET' AND (:tourId IS NULL OR r.tour.id = :tourId)")
    List<UserTreasureStatus> findByUserIdAndCollected(java.util.UUID userId, Long tourId);
}
