package com.app.questofseoul.dto.admin;

public record SpotAdminResponse(
    Long id,
    Long tourId,
    String type,
    String title,
    String description,
    Double latitude,
    Double longitude,
    Integer radiusM,
    Integer orderIndex
) {}
