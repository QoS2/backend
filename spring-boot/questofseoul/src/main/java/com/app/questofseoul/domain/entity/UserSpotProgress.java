package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ProgressStatus;
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

    /** 50m 근접 시 잠금 해제 → progress_status = ACTIVE (lock_state 제거됨, progress_status로 통합) */
    public void unlock() {
        if (this.progressStatus == ProgressStatus.PENDING) {
            this.progressStatus = ProgressStatus.ACTIVE;
            this.unlockedAt = LocalDateTime.now();
        }
    }

    /** 스팟 완료 처리 → progress_status = COMPLETED */
    public void complete() {
        this.progressStatus = ProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /** 미션 스킵 처리 → progress_status = SKIPPED */
    public void skip() {
        this.progressStatus = ProgressStatus.SKIPPED;
    }
}
