package com.app.questofseoul.dto.tour;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NextSpotResponse(
    Long runId,
    String status,
    Boolean hasNextSpot,
    NextSpotDto nextSpot,
    TourDetailResponse.ProgressDto progress
) {
    public record NextSpotDto(
        Long spotId,
        String spotType,
        String title,
        Double lat,
        Double lng,
        Integer radiusM,
        Integer orderIndex
    ) {}
}
