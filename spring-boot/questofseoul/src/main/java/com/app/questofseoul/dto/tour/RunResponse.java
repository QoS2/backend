package com.app.questofseoul.dto.tour;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunResponse {

    private Long runId;
    private Long tourId;
    private String status;
    private String mode;
    private TourDetailResponse.ProgressDto progress;
    private TourDetailResponse.StartSpotDto startSpot;
}
