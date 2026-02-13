package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.TourKnowledgeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourKnowledgeEmbeddingRepository extends JpaRepository<TourKnowledgeEmbedding, Long> {

    void deleteBySourceTypeAndSourceId(String sourceType, Long sourceId);

    void deleteByTourId(Long tourId);

    List<TourKnowledgeEmbedding> findByTourId(Long tourId);
}
