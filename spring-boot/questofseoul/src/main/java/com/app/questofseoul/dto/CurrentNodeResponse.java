package com.app.questofseoul.dto;

import com.app.questofseoul.domain.enums.NodeType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CurrentNodeResponse {
    private UUID nodeId;
    private NodeType nodeType;
    private String title;
    private Double latitude;
    private Double longitude;
    private Boolean hasContent;
}
