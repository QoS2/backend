package com.app.questofseoul.dto.tour;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
    @NotBlank String text
) {}
