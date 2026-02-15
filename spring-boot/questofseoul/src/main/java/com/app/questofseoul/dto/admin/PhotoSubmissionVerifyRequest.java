package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record PhotoSubmissionVerifyRequest(
        @NotBlank(message = "action은 필수입니다 (APPROVE 또는 REJECT)")
        String action,
        String rejectReason
) {}
