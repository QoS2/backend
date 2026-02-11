package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuideContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false, unique = true)
    private Step step;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "guideContent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuideSegment> segments = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static GuideContent create(Step step, String externalKey) {
        GuideContent g = new GuideContent();
        g.step = step;
        g.externalKey = externalKey;
        return g;
    }
}
