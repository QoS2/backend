package com.app.questofseoul.dto.tour;

import com.app.questofseoul.domain.enums.MarkerType;

import java.math.BigDecimal;

public record MarkerResponse(
    Long id,
    MarkerType type,
    String title,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer radiusM,
    Long refId,
    Integer stepOrder
) {}
