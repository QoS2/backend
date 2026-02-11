package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.enums.TourRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourRunRepository extends JpaRepository<TourRun, Long> {
    List<TourRun> findByUserIdOrderByStartedAtDesc(UUID userId);
    Optional<TourRun> findByUserIdAndTourIdAndStatus(UUID userId, Long tourId, TourRunStatus status);
}
