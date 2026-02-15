package com.app.questofseoul.dto.collection;

import java.util.List;

public record TreasureCollectionSummaryResponse(
        List<TreasureSummaryByTourDto> byTour,
        int totalCollected,
        int totalAvailable
) {
    public record TreasureSummaryByTourDto(Long tourId, String tourTitle, int collected, int total) {}
}
