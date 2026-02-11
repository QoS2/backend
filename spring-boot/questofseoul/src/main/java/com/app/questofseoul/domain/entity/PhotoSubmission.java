package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "photo_submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_spot_id", nullable = false)
    private PhotoSpot photoSpot;

    @Column(name = "s3_object_key")
    private String s3ObjectKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private Map<String, Object> metaJson;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }

    public static PhotoSubmission create(User user, PhotoSpot photoSpot, String s3ObjectKey, Map<String, Object> metaJson) {
        PhotoSubmission p = new PhotoSubmission();
        p.user = user;
        p.photoSpot = photoSpot;
        p.s3ObjectKey = s3ObjectKey;
        p.metaJson = metaJson;
        return p;
    }
}
