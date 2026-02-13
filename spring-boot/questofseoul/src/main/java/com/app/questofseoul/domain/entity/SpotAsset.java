package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.SpotAssetUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spot_assets", uniqueConstraints = @UniqueConstraint(columnNames = {"spot_id", "usage", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TourSpot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private MediaAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage", nullable = false)
    private SpotAssetUsage usage;

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

    public static SpotAsset create(TourSpot spot, MediaAsset asset, SpotAssetUsage usage) {
        SpotAsset sa = new SpotAsset();
        sa.spot = spot;
        sa.asset = asset;
        sa.usage = usage;
        return sa;
    }
}
