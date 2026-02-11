package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "segment_media_map")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SegmentMediaMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_segment_id", nullable = false)
    private GuideSegment guideSegment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_asset_id", nullable = false)
    private MediaAsset mediaAsset;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_json", columnDefinition = "jsonb")
    private Map<String, Object> ruleJson;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static SegmentMediaMap create(GuideSegment guideSegment, MediaAsset mediaAsset, Integer sortOrder) {
        SegmentMediaMap m = new SegmentMediaMap();
        m.guideSegment = guideSegment;
        m.mediaAsset = mediaAsset;
        m.sortOrder = sortOrder != null ? sortOrder : 0;
        return m;
    }
}
