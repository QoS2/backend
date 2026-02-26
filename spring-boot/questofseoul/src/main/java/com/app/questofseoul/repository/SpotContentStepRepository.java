package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.enums.StepKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpotContentStepRepository extends JpaRepository<SpotContentStep, Long> {

    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.spot.id = :spotId
              AND s.language = :language
              AND (s.isPublished = true OR s.isPublished IS NULL)
            ORDER BY s.stepIndex ASC, s.id ASC
            """)
    List<SpotContentStep> findBySpotIdAndLanguageOrderByStepIndexAsc(Long spotId, String language);

    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.spot.id = :spotId
              AND s.kind = :kind
              AND s.language = :language
              AND (s.isPublished = true OR s.isPublished IS NULL)
            ORDER BY s.stepIndex ASC, s.id ASC
            """)
    List<SpotContentStep> findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(Long spotId, StepKind kind, String language);

    @Query("""
            SELECT COUNT(s)
            FROM SpotContentStep s
            WHERE s.spot.tour.id = :tourId
              AND s.kind = :kind
              AND (s.isPublished = true OR s.isPublished IS NULL)
              AND (s.spot.isActive = true OR s.spot.isActive IS NULL)
            """)
    long countMissionsByTourId(Long tourId, StepKind kind);

    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.spot.id = :spotId
              AND s.language = :language
              AND (s.isPublished = true OR s.isPublished IS NULL)
            ORDER BY s.stepIndex ASC, s.id ASC
            """)
    List<SpotContentStep> findBySpot_IdAndLanguageOrderByStepIndexAsc(Long spotId, String language);

    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.spot.id = :spotId
              AND s.kind = :kind
              AND s.mission.id = :missionId
              AND (s.isPublished = true OR s.isPublished IS NULL)
            """)
    List<SpotContentStep> findBySpot_IdAndKindAndMission_Id(Long spotId, StepKind kind, Long missionId);

    @Override
    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.id = :id
              AND (s.isPublished = true OR s.isPublished IS NULL)
            """)
    Optional<SpotContentStep> findById(Long id);

    @Query("""
            SELECT s
            FROM SpotContentStep s
            WHERE s.spot.id = :spotId
              AND s.language = :language
            ORDER BY s.stepIndex ASC, s.id ASC
            """)
    List<SpotContentStep> findAllBySpotIdAndLanguageOrderByStepIndexAscIncludingUnpublished(Long spotId, String language);
}
