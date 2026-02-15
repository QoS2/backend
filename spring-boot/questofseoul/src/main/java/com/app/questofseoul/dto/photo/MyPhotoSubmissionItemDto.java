package com.app.questofseoul.dto.photo;

import java.time.LocalDateTime;

public record MyPhotoSubmissionItemDto(
        Long submissionId,
        Long spotId,
        String spotTitle,
        String photoUrl,
        String status,
        LocalDateTime submittedAt,
        String rejectReason,
        LocalDateTime mintedAt
) {}
