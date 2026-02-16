package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.TourAssetUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_assets", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_id", "usage", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private MediaAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage", nullable = false)
    private TourAssetUsage usage;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    @Column(name = "caption")
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static TourAsset create(Tour tour, MediaAsset asset, TourAssetUsage usage) {
        return create(tour, asset, usage, 1);
    }

    public static TourAsset create(Tour tour, MediaAsset asset, TourAssetUsage usage, int sortOrder) {
        TourAsset ta = new TourAsset();
        ta.tour = tour;
        ta.asset = asset;
        ta.usage = usage;
        ta.sortOrder = sortOrder;
        return ta;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
