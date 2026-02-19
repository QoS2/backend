package com.app.questofseoul.dto.tour;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourListItem {
    private Long id;
    private String externalKey;
    private String title;
    private String thumbnailUrl;
    private String description;
    private TourDetailResponse.CountsDto counts;
    private Integer estimatedDurationMin;
    private String accessStatus;
    private List<TourDetailResponse.TagDto> tags;
}
