package com.app.questofseoul.dto.admin;

import java.math.BigDecimal;

public record StepUpdateRequest(
    Integer stepOrder,
    String titleEn,
    String shortDescEn,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer radiusM
) {}
