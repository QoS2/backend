package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {
    Optional<Tour> findByExternalKey(String externalKey);
}
