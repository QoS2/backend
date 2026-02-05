package com.app.questofseoul.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentCompleteResponse {
    private Boolean hasNextContent;
    private Integer nextContentOrder;
    private Boolean nextActionEnabled;
}
