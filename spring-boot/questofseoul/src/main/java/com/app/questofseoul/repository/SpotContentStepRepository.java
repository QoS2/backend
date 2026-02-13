package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.enums.StepKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpotContentStepRepository extends JpaRepository<SpotContentStep, Long> {

    List<SpotContentStep> findBySpotIdAndLanguageOrderByStepIndexAsc(Long spotId, String language);

    List<SpotContentStep> findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(Long spotId, StepKind kind, String language);

    @Query("SELECT COUNT(s) FROM SpotContentStep s WHERE s.spot.tour.id = :tourId AND s.kind = :kind")
    long countMissionsByTourId(Long tourId, StepKind kind);
}
