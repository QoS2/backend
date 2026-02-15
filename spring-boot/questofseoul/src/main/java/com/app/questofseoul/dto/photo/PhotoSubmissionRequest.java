package com.app.questofseoul.dto.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhotoSubmissionRequest(
        @NotBlank(message = "photoUrl은 필수입니다")
        String photoUrl
) {}
