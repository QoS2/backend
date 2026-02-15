package com.app.questofseoul.dto.collection;

import java.time.LocalDateTime;

public record TreasureCollectionItemDto(
        Long spotId,
        Long tourId,
        String tourTitle,
        String title,
        String description,
        String thumbnailUrl,
        LocalDateTime gotAt,
        Integer orderIndex,
        boolean collected
) {}
