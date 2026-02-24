package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spot_content_steps", uniqueConstraints = @UniqueConstraint(columnNames = {"spot_id", "language", "step_index"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotContentStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TourSpot spot;

    @Column(name = "language", nullable = false)
    private String language = "ko";

    @Column(name = "step_index", nullable = false)
    private Integer stepIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private StepKind kind;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_action")
    private StepNextAction nextAction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL)
    private List<SpotScriptLine> scriptLines = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLanguage(String language) {
        if (language != null && !language.isBlank()) {
            this.language = language;
        }
    }

    public void setStepIndex(Integer stepIndex) {
        if (stepIndex != null) {
            this.stepIndex = stepIndex;
        }
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public void setNextAction(StepNextAction nextAction) {
        this.nextAction = nextAction;
    }

    public static SpotContentStep create(TourSpot spot, StepKind kind, int stepIndex) {
        SpotContentStep s = new SpotContentStep();
        s.spot = spot;
        s.kind = kind;
        s.stepIndex = stepIndex;
        return s;
    }
}
