package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StepCreateRequest(
    @NotBlank String externalKey,
    @NotNull Integer stepOrder,
    @NotBlank String titleEn,
    String shortDescEn,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer radiusM
) {}
