package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.Difficulty;
import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.domain.enums.QuestTone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestTheme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestTone tone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "start_location", columnDefinition = "geography(Point,4326)")
    private Point startLocation;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestNode> nodes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static Quest create(String title, String subtitle, QuestTheme theme,
                              QuestTone tone, Difficulty difficulty,
                              Integer estimatedMinutes, Point startLocation) {
        Quest quest = new Quest();
        quest.title = title;
        quest.subtitle = subtitle;
        quest.theme = theme;
        quest.tone = tone;
        quest.difficulty = difficulty;
        quest.estimatedMinutes = estimatedMinutes;
        quest.startLocation = startLocation;
        return quest;
    }

    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setTheme(QuestTheme theme) { this.theme = theme; }
    public void setTone(QuestTone tone) { this.tone = tone; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public void setStartLocation(Point startLocation) { this.startLocation = startLocation; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive != null ? isActive : true; }
}
