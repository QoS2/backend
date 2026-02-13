package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.MediaAsset;
import com.app.questofseoul.domain.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByAssetType(AssetType assetType);
}
