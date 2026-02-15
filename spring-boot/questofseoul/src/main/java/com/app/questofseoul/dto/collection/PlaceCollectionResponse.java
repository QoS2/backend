package com.app.questofseoul.dto.collection;

import java.util.List;

public record PlaceCollectionResponse(
        int totalCollected,
        int totalAvailable,
        List<PlaceCollectionItemDto> items
) {}
