package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByExternalKey(String externalKey);
}
