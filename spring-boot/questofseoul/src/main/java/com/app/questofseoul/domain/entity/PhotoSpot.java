package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private Step step;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "desc_en", columnDefinition = "TEXT")
    private String descEn;

    @Column(precision = 18, scale = 12)
    private BigDecimal latitude;

    @Column(precision = 18, scale = 12)
    private BigDecimal longitude;

    @Column(name = "radius_m")
    private Integer radiusM = 50;

    @Column(name = "mint_reward")
    private Integer mintReward = 0;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static PhotoSpot create(Tour tour, Step step, String externalKey, String titleEn,
                                   String descEn, BigDecimal latitude, BigDecimal longitude,
                                   Integer radiusM, Integer mintReward) {
        PhotoSpot p = new PhotoSpot();
        p.tour = tour;
        p.step = step;
        p.externalKey = externalKey;
        p.titleEn = titleEn;
        p.descEn = descEn;
        p.latitude = latitude;
        p.longitude = longitude;
        p.radiusM = radiusM != null ? radiusM : 50;
        p.mintReward = mintReward != null ? mintReward : 0;
        return p;
    }
}
