package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Step;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findByTourIdOrderByStepOrderAsc(Long tourId);
    Optional<Step> findByExternalKey(String externalKey);
}
