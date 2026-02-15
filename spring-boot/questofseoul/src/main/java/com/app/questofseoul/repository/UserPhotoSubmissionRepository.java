package com.app.questofseoul.repository;

import com.app.questofseoul.domain.entity.UserPhotoSubmission;
import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPhotoSubmissionRepository extends JpaRepository<UserPhotoSubmission, Long> {

    List<UserPhotoSubmission> findByUser_IdOrderBySubmittedAtDesc(UUID userId);

    List<UserPhotoSubmission> findByUser_IdAndSpot_IdOrderBySubmittedAtDesc(UUID userId, Long spotId);

    List<UserPhotoSubmission> findByUser_IdAndStatusOrderBySubmittedAtDesc(UUID userId, PhotoSubmissionStatus status);

    List<UserPhotoSubmission> findByUser_IdAndSpot_IdAndStatusOrderBySubmittedAtDesc(UUID userId, Long spotId, PhotoSubmissionStatus status);

    List<UserPhotoSubmission> findByStatusOrderBySubmittedAtAsc(PhotoSubmissionStatus status);

    List<UserPhotoSubmission> findBySpot_IdAndStatusOrderBySubmittedAtDesc(Long spotId, PhotoSubmissionStatus status);

    long countBySpotIdAndStatus(Long spotId, PhotoSubmissionStatus status);
}
