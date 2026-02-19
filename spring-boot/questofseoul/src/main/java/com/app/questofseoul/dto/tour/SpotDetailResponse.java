package com.app.questofseoul.dto.tour;

public record SpotDetailResponse(
        Long spotId,
        String type,
        String title,
        String titleKr,
        String description,
        String pronunciationUrl,
        String thumbnailUrl,
        Double lat,
        Double lng,
        String address
) {}
