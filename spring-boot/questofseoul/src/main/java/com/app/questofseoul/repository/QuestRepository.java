package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.Quest;
import com.app.questofseoul.domain.enums.QuestTheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    List<Quest> findByIsActiveTrue();

    Page<Quest> findByIsActive(Boolean isActive, Pageable pageable);
    Page<Quest> findByTheme(QuestTheme theme, Pageable pageable);
    Page<Quest> findByIsActiveAndTheme(Boolean isActive, QuestTheme theme, Pageable pageable);
    Page<Quest> findAllBy(Pageable pageable);
}
