package com.app.questofseoul.dto.collection;

import java.util.List;

public record PlaceCollectionSummaryResponse(
        List<PlaceSummaryByTourDto> byTour,
        int totalCollected,
        int totalAvailable
) {
    public record PlaceSummaryByTourDto(Long tourId, String tourTitle, int collected, int total) {}
}
