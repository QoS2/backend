package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.SpotScriptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpotScriptLineRepository extends JpaRepository<SpotScriptLine, Long> {

    List<SpotScriptLine> findByStep_IdOrderBySeqAsc(Long stepId);
}
