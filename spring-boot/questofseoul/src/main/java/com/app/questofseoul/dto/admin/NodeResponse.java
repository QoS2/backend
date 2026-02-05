package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.NodeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class NodeResponse {
    private UUID id;
    private UUID questId;
    private NodeType nodeType;
    private String title;
    private Integer orderIndex;
    private Double geoLatitude;
    private Double geoLongitude;
    private Map<String, Object> unlockCondition;
    private LocalDateTime createdAt;
}
