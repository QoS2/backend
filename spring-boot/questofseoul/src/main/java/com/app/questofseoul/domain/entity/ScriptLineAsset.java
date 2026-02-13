package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.LineAssetUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "script_line_assets", uniqueConstraints = @UniqueConstraint(columnNames = {"script_line_id", "usage", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScriptLineAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_line_id", nullable = false)
    private SpotScriptLine scriptLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private MediaAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage", nullable = false)
    private LineAssetUsage usage;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public static ScriptLineAsset create(SpotScriptLine line, MediaAsset asset, LineAssetUsage usage) {
        ScriptLineAsset sla = new ScriptLineAsset();
        sla.scriptLine = line;
        sla.asset = asset;
        sla.usage = usage;
        return sla;
    }
}
