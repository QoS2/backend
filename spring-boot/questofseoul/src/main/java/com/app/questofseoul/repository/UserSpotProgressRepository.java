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
}
