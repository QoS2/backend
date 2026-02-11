package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.ChatContent;
import com.app.questofseoul.domain.enums.ChatRefType;
import com.app.questofseoul.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatContentRepository extends JpaRepository<ChatContent, Long> {
    Optional<ChatContent> findByRefTypeAndRefIdAndLanguage(ChatRefType refType, Long refId, Language language);
}
