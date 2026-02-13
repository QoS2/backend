package com.app.questofseoul.dto.admin;

public record SpotUpdateRequest(
    String title,
    String description,
    Integer orderIndex,
    Double latitude,
    Double longitude,
    Integer radiusM
) {}
