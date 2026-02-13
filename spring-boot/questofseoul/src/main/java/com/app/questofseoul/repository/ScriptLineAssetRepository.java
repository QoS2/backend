package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ScriptLineAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScriptLineAssetRepository extends JpaRepository<ScriptLineAsset, Long> {

    List<ScriptLineAsset> findByScriptLine_IdOrderBySortOrderAsc(Long scriptLineId);
}
