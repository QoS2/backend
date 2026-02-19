package com.app.questofseoul.dto.tour;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProximityRequest(
    @NotNull BigDecimal lat,
    @NotNull BigDecimal lng
) {}
