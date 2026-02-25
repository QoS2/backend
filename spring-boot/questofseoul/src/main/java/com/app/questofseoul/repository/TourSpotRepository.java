package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TourSpotRepository extends JpaRepository<TourSpot, Long> {

    @Query("""
            SELECT s
            FROM TourSpot s
            WHERE s.tour.id = :tourId
              AND (s.isActive = true OR s.isActive IS NULL)
            ORDER BY s.orderIndex ASC, s.id ASC
            """)
    List<TourSpot> findByTourIdOrderByOrderIndexAsc(@Param("tourId") Long tourId);

    @Query("""
            SELECT s
            FROM TourSpot s
            WHERE s.tour.id = :tourId
              AND s.type = :type
              AND (s.isActive = true OR s.isActive IS NULL)
            ORDER BY s.orderIndex ASC, s.id ASC
            """)
    List<TourSpot> findByTourIdAndTypeOrderByOrderIndexAsc(@Param("tourId") Long tourId, @Param("type") SpotType type);

    @Query("""
            SELECT s
            FROM TourSpot s
            JOIN FETCH s.tour t
            WHERE (:tourId IS NULL OR t.id = :tourId)
              AND (s.isActive = true OR s.isActive IS NULL)
              AND s.type IN :types
            ORDER BY t.id ASC, s.orderIndex ASC, s.id ASC
            """)
    List<TourSpot> findCollectibleSpotsByTourIdAndTypes(@Param("tourId") Long tourId,
                                                        @Param("types") Collection<SpotType> types);

    @Query("SELECT COUNT(s) FROM TourSpot s WHERE s.tour.id = :tourId AND s.type = :type AND (s.isActive = true OR s.isActive IS NULL)")
    long countByTourIdAndType(Long tourId, SpotType type);

    @Query("""
            SELECT s
            FROM TourSpot s
            WHERE s.id = :id
              AND s.tour.id = :tourId
              AND (s.isActive = true OR s.isActive IS NULL)
            """)
    Optional<TourSpot> findByIdAndTourId(@Param("id") Long id, @Param("tourId") Long tourId);

    @Override
    @Query("""
            SELECT s
            FROM TourSpot s
            WHERE s.id = :id
              AND (s.isActive = true OR s.isActive IS NULL)
            """)
    Optional<TourSpot> findById(@Param("id") Long id);
}
