package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.SpotAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotAssetRepository extends JpaRepository<SpotAsset, Long> {

    boolean existsByAsset_Id(Long assetId);
}
