package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.TourAccessMethod;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tour_access", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tour_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTourAccess {

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
    @Column(name = "status", nullable = false)
    private TourAccessStatus status = TourAccessStatus.LOCKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private TourAccessMethod method = TourAccessMethod.FREE;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (grantedAt == null) grantedAt = LocalDateTime.now();
    }

    public static UserTourAccess create(User user, Tour tour) {
        UserTourAccess a = new UserTourAccess();
        a.user = user;
        a.tour = tour;
        a.status = TourAccessStatus.LOCKED;
        return a;
    }

    public void unlock() {
        this.status = TourAccessStatus.UNLOCKED;
    }
}
