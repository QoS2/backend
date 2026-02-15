package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserSpotProgress;
import com.app.questofseoul.domain.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserSpotProgressRepository extends JpaRepository<UserSpotProgress, Long> {

    List<UserSpotProgress> findByTourRunId(Long tourRunId);

    Optional<UserSpotProgress> findByTourRunIdAndSpotId(Long tourRunId, Long spotId);

    @Query("SELECT COUNT(p) FROM UserSpotProgress p WHERE p.tourRun.id = :runId AND p.progressStatus = :status")
    long countByTourRunIdAndProgressStatus(Long runId, ProgressStatus status);

    @Query("SELECT p FROM UserSpotProgress p JOIN FETCH p.spot s JOIN FETCH p.tourRun r JOIN FETCH r.tour WHERE r.user.id = :userId AND p.lockState = 'UNLOCKED' AND s.type IN ('MAIN','SUB') AND (:tourId IS NULL OR r.tour.id = :tourId)")
    List<UserSpotProgress> findByUserIdAndUnlockedPlaces(java.util.UUID userId, Long tourId);
}
