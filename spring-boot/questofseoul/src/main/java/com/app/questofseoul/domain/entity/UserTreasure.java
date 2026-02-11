package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_treasures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTreasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treasure_id", nullable = false)
    private Treasure treasure;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @PrePersist
    protected void onCreate() {
        claimedAt = LocalDateTime.now();
    }

    public static UserTreasure create(User user, Treasure treasure) {
        UserTreasure ut = new UserTreasure();
        ut.user = user;
        ut.treasure = treasure;
        return ut;
    }
}
