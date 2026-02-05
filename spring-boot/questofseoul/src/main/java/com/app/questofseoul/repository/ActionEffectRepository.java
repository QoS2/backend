package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ActionEffect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionEffectRepository extends JpaRepository<ActionEffect, UUID> {
    List<ActionEffect> findByActionId(UUID actionId);

    boolean existsByActionIdAndId(UUID actionId, UUID effectId);
}
