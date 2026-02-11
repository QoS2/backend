package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.TourRunStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tour_runs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourRunStatus status = TourRunStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "tourRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSession> chatSessions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    public static TourRun create(User user, Tour tour) {
        TourRun r = new TourRun();
        r.user = user;
        r.tour = tour;
        r.status = TourRunStatus.ACTIVE;
        r.startedAt = LocalDateTime.now();
        return r;
    }
}
