package com.app.questofseoul.dto.admin;

import java.math.BigDecimal;

public record StepAdminResponse(
    Long id,
    String externalKey,
    Long tourId,
    Integer stepOrder,
    String titleEn,
    String shortDescEn,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer radiusM
) {}
