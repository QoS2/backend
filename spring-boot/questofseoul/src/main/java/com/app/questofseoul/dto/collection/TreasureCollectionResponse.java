package com.app.questofseoul.dto.collection;

import java.util.List;

public record TreasureCollectionResponse(
        int totalCollected,
        int totalAvailable,
        List<TreasureCollectionItemDto> items
) {}
