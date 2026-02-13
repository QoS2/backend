package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserMissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMissionAttemptRepository extends JpaRepository<UserMissionAttempt, Long> {

    List<UserMissionAttempt> findByTourRun_IdAndStep_Id(Long tourRunId, Long stepId);

    Optional<UserMissionAttempt> findByTourRun_IdAndStep_IdAndAttemptNo(Long tourRunId, Long stepId, Integer attemptNo);
}
