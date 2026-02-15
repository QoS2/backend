package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.SpotAsset;
import com.app.questofseoul.domain.enums.SpotAssetUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpotAssetRepository extends JpaRepository<SpotAsset, Long> {

    boolean existsByAsset_Id(Long assetId);

    List<SpotAsset> findBySpot_IdOrderBySortOrderAsc(Long spotId);

    Optional<SpotAsset> findFirstBySpot_IdAndUsageOrderBySortOrderAsc(Long spotId, SpotAssetUsage usage);
}
