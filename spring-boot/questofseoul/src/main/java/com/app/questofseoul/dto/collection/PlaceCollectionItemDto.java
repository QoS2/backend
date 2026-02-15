package com.app.questofseoul.dto.collection;

import java.time.LocalDateTime;

public record PlaceCollectionItemDto(
        Long spotId,
        Long tourId,
        String tourTitle,
        String type,
        String title,
        String description,
        String thumbnailUrl,
        LocalDateTime collectedAt,
        Integer orderIndex,
        boolean collected
) {}
