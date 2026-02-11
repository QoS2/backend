package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "waypoints")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waypoint {

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

    @Column(name = "message_en", columnDefinition = "TEXT")
    private String messageEn;

    @Column(precision = 18, scale = 12)
    private BigDecimal latitude;

    @Column(precision = 18, scale = 12)
    private BigDecimal longitude;

    @Column(name = "radius_m")
    private Integer radiusM = 50;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_asset_id")
    private MediaAsset mediaAsset;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Waypoint create(Tour tour, Step step, String externalKey, String titleEn,
                                  String messageEn, BigDecimal latitude, BigDecimal longitude, Integer radiusM) {
        Waypoint w = new Waypoint();
        w.tour = tour;
        w.step = step;
        w.externalKey = externalKey;
        w.titleEn = titleEn;
        w.messageEn = messageEn;
        w.latitude = latitude;
        w.longitude = longitude;
        w.radiusM = radiusM != null ? radiusM : 50;
        return w;
    }
}
