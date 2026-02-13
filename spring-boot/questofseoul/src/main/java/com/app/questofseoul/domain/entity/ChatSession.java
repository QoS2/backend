package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_run_id", "spot_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_run_id", nullable = false)
    private TourRun tourRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TourSpot spot;

    @Column(name = "language", nullable = false)
    private String language = "ko";

    @Column(name = "allow_user_question", nullable = false)
    private Boolean allowUserQuestion = false;

    @Column(name = "cursor_step_index", nullable = false)
    private Integer cursorStepIndex = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<ChatTurn> turns = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        lastActivityAt = now;
    }

    public static ChatSession create(TourRun tourRun, TourSpot spot) {
        ChatSession s = new ChatSession();
        s.tourRun = tourRun;
        s.spot = spot;
        s.allowUserQuestion = spot.getAiChatEnabled() != null && spot.getAiChatEnabled();
        return s;
    }
}
