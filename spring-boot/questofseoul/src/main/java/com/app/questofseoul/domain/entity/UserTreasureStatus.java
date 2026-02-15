package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.TreasureStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_treasure_status", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_run_id", "treasure_spot_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTreasureStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_run_id", nullable = false)
    private TourRun tourRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasure_spot_id", nullable = false)
    private TourSpot treasureSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TreasureStatus status = TreasureStatus.LOCKED;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "got_at")
    private LocalDateTime gotAt;

    @PrePersist
    protected void onCreate() {
    }

    public static UserTreasureStatus create(TourRun run, TourSpot treasureSpot) {
        UserTreasureStatus s = new UserTreasureStatus();
        s.tourRun = run;
        s.treasureSpot = treasureSpot;
        return s;
    }

    /** 50m 근접 시 잠금 해제 (발견) */
    public void unlock() {
        if (this.status == TreasureStatus.LOCKED) {
            this.status = TreasureStatus.UNLOCKED;
            this.unlockedAt = LocalDateTime.now();
        }
    }

    /** Collect Treasure 클릭 시 수집 완료 */
    public void collect() {
        this.status = TreasureStatus.GET;
        this.gotAt = LocalDateTime.now();
    }
}
