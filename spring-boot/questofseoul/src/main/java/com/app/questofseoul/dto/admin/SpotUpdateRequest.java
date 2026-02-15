package com.app.questofseoul.dto.admin;

public record SpotUpdateRequest(
    String title,
    String titleKr,
    String description,
    String pronunciationUrl,
    String address,
    Integer orderIndex,
    Double latitude,
    Double longitude,
    Integer radiusM
) {}
