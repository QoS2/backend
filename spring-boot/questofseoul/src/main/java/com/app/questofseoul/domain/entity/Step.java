package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "steps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "short_desc_en", columnDefinition = "TEXT")
    private String shortDescEn;

    @Column(precision = 18, scale = 12)
    private BigDecimal latitude;

    @Column(precision = 18, scale = 12)
    private BigDecimal longitude;

    @Column(name = "radius_m")
    private Integer radiusM = 50;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public void setShortDescEn(String shortDescEn) { this.shortDescEn = shortDescEn; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public void setRadiusM(Integer radiusM) { this.radiusM = radiusM != null ? radiusM : 50; }

    public static Step create(Tour tour, String externalKey, Integer stepOrder, String titleEn,
                              String shortDescEn, BigDecimal latitude, BigDecimal longitude, Integer radiusM) {
        Step s = new Step();
        s.tour = tour;
        s.externalKey = externalKey;
        s.stepOrder = stepOrder;
        s.titleEn = titleEn;
        s.shortDescEn = shortDescEn;
        s.latitude = latitude;
        s.longitude = longitude;
        s.radiusM = radiusM != null ? radiusM : 50;
        return s;
    }
}
