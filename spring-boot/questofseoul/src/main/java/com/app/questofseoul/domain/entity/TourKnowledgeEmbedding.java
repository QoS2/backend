package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * RAG용 투어 지식 임베딩. Tour, TourSpot, SpotScriptLine 등의 콘텐츠를 임베딩하여 저장.
 * ai-server와 Spring Boot가 동일한 Pgvector 테이블을 공유한다.
 */
@Entity
@Table(name = "tour_knowledge_embeddings", indexes = {
    @Index(name = "idx_tke_source", columnList = "source_type, source_id"),
    @Index(name = "idx_tke_tour", columnList = "tour_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourKnowledgeEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "tour_id")
    private Long tourId;

    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "embedding", nullable = false, columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static TourKnowledgeEmbedding create(String sourceType, Long sourceId, Long tourId, Long spotId,
                                                String content, String title, float[] embedding) {
        TourKnowledgeEmbedding e = new TourKnowledgeEmbedding();
        e.sourceType = sourceType;
        e.sourceId = sourceId;
        e.tourId = tourId;
        e.spotId = spotId;
        e.content = content;
        e.title = title;
        e.embedding = embedding;
        return e;
    }
}
