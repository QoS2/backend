package com.app.questofseoul.dto.tour;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProximityRequest(
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude
) {}
