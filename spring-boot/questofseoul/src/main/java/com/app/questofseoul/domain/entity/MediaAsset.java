package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.MediaAssetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "media_assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaAssetType type;

    @Column(name = "url_or_key", nullable = false)
    private String urlOrKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private Map<String, Object> metaJson;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static MediaAsset create(String externalKey, MediaAssetType type,
                                    String urlOrKey, Map<String, Object> metaJson) {
        MediaAsset e = new MediaAsset();
        e.externalKey = externalKey;
        e.type = type;
        e.urlOrKey = urlOrKey;
        e.metaJson = metaJson;
        return e;
    }
}
