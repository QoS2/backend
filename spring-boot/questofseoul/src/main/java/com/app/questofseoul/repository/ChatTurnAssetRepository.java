package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatTurnAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatTurnAssetRepository extends JpaRepository<ChatTurnAsset, Long> {

    boolean existsByAsset_Id(Long assetId);
}
