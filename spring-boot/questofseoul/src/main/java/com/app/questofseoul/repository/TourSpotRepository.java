package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TourSpotRepository extends JpaRepository<TourSpot, Long> {

    List<TourSpot> findByTourIdOrderByOrderIndexAsc(Long tourId);

    List<TourSpot> findByTourIdAndTypeOrderByOrderIndexAsc(Long tourId, SpotType type);

    @Query("SELECT COUNT(s) FROM TourSpot s WHERE s.tour.id = :tourId AND s.type = :type")
    long countByTourIdAndType(Long tourId, SpotType type);

    Optional<TourSpot> findByIdAndTourId(Long id, Long tourId);
}
