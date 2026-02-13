package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ProgressStatus;
import com.app.questofseoul.domain.enums.SpotLockState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_spot_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_run_id", "spot_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpotProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_run_id", nullable = false)
    private TourRun tourRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TourSpot spot;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_state", nullable = false)
    private SpotLockState lockState = SpotLockState.LOCKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status", nullable = false)
    private ProgressStatus progressStatus = ProgressStatus.PENDING;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static UserSpotProgress create(TourRun run, TourSpot spot) {
        UserSpotProgress p = new UserSpotProgress();
        p.tourRun = run;
        p.spot = spot;
        return p;
    }
}
