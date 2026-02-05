package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.QuestNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestNodeRepository extends JpaRepository<QuestNode, UUID> {
    List<QuestNode> findByQuestIdOrderByOrderIndex(UUID questId);
    
    @Query("SELECT n FROM QuestNode n WHERE n.quest.id = :questId AND n.orderIndex = :orderIndex")
    QuestNode findByQuestIdAndOrderIndex(@Param("questId") UUID questId,
                                        @Param("orderIndex") Integer orderIndex);

    boolean existsByQuestIdAndId(UUID questId, UUID nodeId);
}
