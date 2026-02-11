package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.GuideContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuideContentRepository extends JpaRepository<GuideContent, Long> {
    Optional<GuideContent> findByStepId(Long stepId);
}
