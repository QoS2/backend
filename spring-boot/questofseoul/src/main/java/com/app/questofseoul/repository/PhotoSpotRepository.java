package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.PhotoSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoSpotRepository extends JpaRepository<PhotoSpot, Long> {
    List<PhotoSpot> findByTourId(Long tourId);
}
