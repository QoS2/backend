package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.NodeContent;
import com.app.questofseoul.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeContentRepository extends JpaRepository<NodeContent, UUID> {
    List<NodeContent> findByNodeIdAndLanguageOrderByContentOrder(UUID nodeId, Language language);

    List<NodeContent> findByNodeIdOrderByContentOrder(UUID nodeId);

    boolean existsByNodeIdAndId(UUID nodeId, UUID contentId);
}
