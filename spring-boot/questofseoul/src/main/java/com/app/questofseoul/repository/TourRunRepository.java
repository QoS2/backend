package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.enums.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourRunRepository extends JpaRepository<TourRun, Long> {

    Optional<TourRun> findByUserIdAndTourIdAndStatus(UUID userId, Long tourId, RunStatus status);

    List<TourRun> findByUserIdAndTourIdOrderByStartedAtDesc(UUID userId, Long tourId);
}
