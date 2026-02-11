package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_segments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuideSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_content_id", nullable = false)
    private GuideContent guideContent;

    @Column(name = "seg_idx", nullable = false)
    private Integer segIdx;

    @Column(name = "text_en", columnDefinition = "TEXT")
    private String textEn;

    @Column(name = "trigger_key")
    private String triggerKey;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "guideSegment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SegmentMediaMap> mediaMaps = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static GuideSegment create(GuideContent guideContent, Integer segIdx, String textEn, String triggerKey) {
        GuideSegment s = new GuideSegment();
        s.guideContent = guideContent;
        s.segIdx = segIdx;
        s.textEn = textEn;
        s.triggerKey = triggerKey;
        return s;
    }
}
