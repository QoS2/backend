package com.app.questofseoul.dto.photo;

import java.time.LocalDateTime;

public record PhotoSubmissionResponse(
        Long submissionId,
        String status,
        String message
) {}
