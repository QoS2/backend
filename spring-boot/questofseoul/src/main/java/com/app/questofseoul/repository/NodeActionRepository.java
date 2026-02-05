package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.NodeAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeActionRepository extends JpaRepository<NodeAction, UUID> {
    List<NodeAction> findByNodeId(UUID nodeId);

    boolean existsByNodeIdAndId(UUID nodeId, UUID actionId);
}
