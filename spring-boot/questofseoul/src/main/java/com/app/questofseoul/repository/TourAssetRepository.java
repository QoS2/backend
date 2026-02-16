package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourAsset;
import com.app.questofseoul.domain.enums.TourAssetUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourAssetRepository extends JpaRepository<TourAsset, Long> {

    List<TourAsset> findByTour_IdOrderBySortOrderAsc(Long tourId);

    Optional<TourAsset> findFirstByTour_IdAndUsageOrderBySortOrderAsc(Long tourId, TourAssetUsage usage);
}
