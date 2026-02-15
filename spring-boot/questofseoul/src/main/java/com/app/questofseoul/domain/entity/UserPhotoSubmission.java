package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_photo_submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPhotoSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private TourSpot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private MediaAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PhotoSubmissionStatus status = PhotoSubmissionStatus.PENDING;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "mint_token")
    private String mintToken;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public static UserPhotoSubmission create(User user, TourSpot spot, MediaAsset asset) {
        UserPhotoSubmission s = new UserPhotoSubmission();
        s.user = user;
        s.spot = spot;
        s.asset = asset;
        s.submittedAt = LocalDateTime.now();
        return s;
    }

    public void approve(UUID verifiedBy, String mintToken) {
        this.status = PhotoSubmissionStatus.APPROVED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
        this.isPublic = true;
        this.mintToken = mintToken;
    }

    public void reject(UUID verifiedBy, String reason) {
        this.status = PhotoSubmissionStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
        this.rejectReason = reason;
    }
}
