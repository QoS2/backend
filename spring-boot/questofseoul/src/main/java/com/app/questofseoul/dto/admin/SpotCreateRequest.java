package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpotCreateRequest(
    @NotBlank String type,
    @NotBlank String title,
    String description,
    Double latitude,
    Double longitude,
    @NotNull Integer orderIndex,
    Integer radiusM
) {}
