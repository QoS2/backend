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
    private PreviousRunDto previousRun;
    private TourDetailResponse.StartSpotDto startSpot;

    @Data
    @Builder
    public static class PreviousRunDto {
        private Long runId;
        private String finalStatus;
    }
}
