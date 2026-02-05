package com.app.questofseoul.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArrivalCheckResponse {
    private Boolean isArrived;
    private Double distanceMeters;
    private Boolean canStart;
}
