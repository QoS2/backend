package com.app.questofseoul.dto.admin;

public record SpotAdminResponse(
    Long id,
    Long tourId,
    String type,
    String title,
    String titleKr,
    String description,
    String pronunciationUrl,
    String address,
    Double latitude,
    Double longitude,
    Integer radiusM,
    Integer orderIndex
) {}
