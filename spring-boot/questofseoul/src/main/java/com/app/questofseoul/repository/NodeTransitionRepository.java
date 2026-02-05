package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.NodeTransition;
import com.app.questofseoul.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeTransitionRepository extends JpaRepository<NodeTransition, UUID> {
    List<NodeTransition> findByFromNodeIdAndToNodeIdAndLanguageOrderByTransitionOrder(
        UUID fromNodeId, UUID toNodeId, Language language);

    @Query("SELECT t FROM NodeTransition t WHERE t.fromNode.quest.id = :questId ORDER BY t.fromNode.orderIndex, t.transitionOrder")
    List<NodeTransition> findByQuestId(@Param("questId") UUID questId);

    @Query("SELECT t FROM NodeTransition t WHERE t.fromNode.id = :nodeId ORDER BY t.transitionOrder")
    List<NodeTransition> findByFromNodeId(@Param("nodeId") UUID nodeId);

    @Query("SELECT t FROM NodeTransition t WHERE t.toNode.id = :nodeId ORDER BY t.transitionOrder")
    List<NodeTransition> findByToNodeId(@Param("nodeId") UUID nodeId);
}
